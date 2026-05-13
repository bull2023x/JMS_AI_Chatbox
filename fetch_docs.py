import re
import time
from pathlib import Path
from urllib.parse import urlparse

import requests
from bs4 import BeautifulSoup

URL_FILE = Path("urls.txt")
OUT_DIR = Path("jms-docs")
OUT_DIR.mkdir(exist_ok=True)


def make_filename(url: str) -> str:
    name = Path(urlparse(url).path).stem
    name = re.sub(r"[^a-zA-Z0-9_-]+", "-", name)
    return f"{name}.txt"


def clean_text(text: str) -> str:
    lines = []
    for line in text.splitlines():
        line = line.strip()
        if line:
            lines.append(line)
    return "\n".join(lines)


def extract_text(html_bytes: bytes) -> str:
    soup = BeautifulSoup(html_bytes, "lxml", from_encoding="utf-8")

    for tag in soup(["script", "style", "nav", "footer", "header", "aside"]):
        tag.decompose()

    main = (
        soup.find("main")
        or soup.find("article")
        or soup.find(id="maincontent")
        or soup.find(class_="main-content")
        or soup.body
    )

    if main is None:
        return ""

    title = ""
    if soup.title and soup.title.string:
        title = soup.title.string.strip()

    body = main.get_text(separator="\n")
    text = f"# {title}\n\n{body}" if title else body
    return clean_text(text)


def main():
    if not URL_FILE.exists():
        raise FileNotFoundError(f"{URL_FILE} does not exist.")

    urls = [
        line.strip()
        for line in URL_FILE.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.startswith("#")
    ]

    if not urls:
        print("No URLs found in urls.txt")
        return

    for url in urls:
        print(f"Fetching: {url}")

        response = requests.get(
            url,
            timeout=30,
            headers={"User-Agent": "jms-aichat-poc/1.0"},
        )
        response.raise_for_status()

        text = extract_text(response.content)
        output_file = OUT_DIR / make_filename(url)

        output_file.write_text(
            f"Source URL: {url}\n\n{text}\n",
            encoding="utf-8",
        )

        print(f"Saved: {output_file}")
        time.sleep(1)


if __name__ == "__main__":
    main()
