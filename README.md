# Java Management Service Assistant

Java Management Service Assistant は、Oracle Java Management Service（JMS）の公開ドキュメントをもとに質問応答を行う、PoC用途のAIチャットアプリケーションです。

このアプリケーションは、Helidon、LangChain4j、Ollama、RAG（Retrieval-Augmented Generation）を組み合わせて構成されています。

OpenAI APIは使用しません。  
無料でローカル実行できる **Ollama** をLLMとして利用します。

## このリポジトリの目的

このリポジトリは、Oracle Java Management Serviceのドキュメントをローカルに取得し、その内容をもとにAIチャットで質問できるPoCです。

たとえば、以下のような質問に回答できます。

```text
Oracle Java Management Serviceとは何ですか？
フリート管理とは何ですか？
Java Downloadとは何ですか？
管理対象インスタンスとは何ですか？
Javaランタイムとは何ですか？
```

このPoCにおける **JMS** は、**Oracle Java Management Service** を意味します。  
一般的な **Java Message Service** ではありません。

## 他のドキュメントへの応用

このPoCは、JMSドキュメントを例にしたRAGチャットボックスです。

ログイン不要の公開HTMLドキュメントであれば、`urls.txt` のURLリストを差し替えるだけで、別テーマの相談チャットボックスとして利用できます。

```bash
python fetch_docs.py
mvn clean package
java -jar target/helidon-assistant.jar

## 主な特徴

- Java 21 + Helidon SE 4 による軽量なWebアプリケーション
- LangChain4jによるAI連携
- OllamaによるローカルLLM実行
- OpenAI APIキー不要
- Oracle Java Management Serviceドキュメントを使ったRAG
- ローカルEmbeddingモデルを使用
- ブラウザで利用できるチャットUI
- PoC、デモ、学習用途向け

## アーキテクチャ概要

このアプリケーションは、以下のような構成です。

```text
Browser UI
  ↓
Helidon REST Service
  ↓
LangChain4j AI Service
  ↓
RAG Content Retriever
  ↓
Embedding Store
  ↓
Local JMS Documents
  ↓
Ollama LLM
```

処理の流れは以下です。

```text
1. Oracle DocsからJMSドキュメントを取得
2. HTML本文をテキスト化
3. jms-docs/*.txt に保存
4. アプリ起動時にテキストを読み込み
5. TextPreprocessorでチャンク化
6. Embedding Storeに格納
7. ユーザー質問に関連するチャンクを検索
8. Ollama上のLLMがドキュメントを参照して回答
```

## 本リポジトリの構成

このリポジトリは、Oracle Java Management Service（JMS）の公開ドキュメントをローカルに取得し、その内容をもとにAIチャットで質問応答を行うPoCです。

構成は以下です。

| 項目 | 内容 |
|---|---|
| Assistant名 | Java Management Service Assistant |
| 対象ドキュメント | Oracle Java Management Service Documentation |
| 対象言語 | 日本語ドキュメント |
| LLM実行環境 | Ollama |
| 利用モデル例 | llama3.2 |
| OpenAI APIキー | 不要 |
| 入力ファイル形式 | Text `.txt` |
| ドキュメント取得方法 | `fetch_docs.py` でOracle Docsから取得 |
| ドキュメント保存先 | `jms-docs/` |
| RAG前処理 | `TextPreprocessor` |
| Web UI | `http://localhost:8080/ui` |
| 主な用途 | PoC、デモ、学習、RAG検証 |

このリポジトリには、Oracle Docs本文は同梱していません。  
利用者が `fetch_docs.py` を実行することで、`urls.txt` に記載されたOracle Docs URLから日本語テキストをローカル生成します。

## 前提条件

以下が必要です。

- Java 21
- Maven
- Git
- Python 3
- Ollama

確認コマンド:

```bash
java --version
mvn --version
git --version
python3 --version
ollama --version
```

## 1. リポジトリを取得する

```bash
git clone https://github.com/bull2023x/JMS_AI_Chatbox.git
cd JMS_AI_Chatbox
```

## 2. Ollamaを準備する

このPoCではOpenAI APIではなく、ローカルLLM実行環境であるOllamaを使います。

Macの場合:

```bash
brew install ollama
```

Ollamaサーバーを起動します。

```bash
ollama serve
```

すでにOllamaが起動している場合、以下のようなメッセージが出ることがあります。

```text
listen tcp 127.0.0.1:11434: bind: address already in use
```

これは通常、Ollamaがすでに起動済みであることを意味します。

起動確認:

```bash
curl http://localhost:11434/api/tags
```

## 3. LLMモデルを取得する

このPoCでは例として `llama3.2` を使います。

```bash
ollama pull llama3.2
```

動作確認:

```bash
ollama run llama3.2
```

以下のように質問して、返答があればOllama側は準備完了です。

```text
こんにちは
```

終了する場合は `/bye` を入力します。

## 4. Python環境を準備する

Oracle Java Management Serviceドキュメントを取得してテキスト化するために、Pythonスクリプトを使います。

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install requests beautifulsoup4 lxml
```

## 5. JMSドキュメントを取得する

このリポジトリには、Oracle Java Management Serviceドキュメント本文は同梱していません。

理由は、ドキュメント本文をリポジトリに再配布するのではなく、利用者が公式Oracle Docsから取得する形にするためです。

取得対象URLは `urls.txt` に記載されています。

```text
urls.txt
```

現在の対象例:

```text
https://docs.oracle.com/ja-jp/iaas/jms/doc/overview-java-management-service.html
https://docs.oracle.com/ja-jp/iaas/jms/doc/java-management-service.html
https://docs.oracle.com/ja-jp/iaas/jms/doc/key-concepts-terminology.html
```

ドキュメントを取得します。

```bash
python fetch_docs.py
```

成功すると、以下のように `jms-docs/` 配下に `.txt` ファイルが生成されます。

```text
jms-docs/
├── overview-java-management-service.txt
├── java-management-service.txt
└── key-concepts-terminology.txt
```

取得結果を確認します。

```bash
ls -l jms-docs
head -40 jms-docs/overview-java-management-service.txt
wc -c jms-docs/*.txt
```

日本語が文字化けせず表示されれば成功です。

## 6. JMSドキュメント取得の仕組み

`fetch_docs.py` は以下を行います。

```text
1. urls.txt からOracle DocsのURLを読み込む
2. 各HTMLページを取得する
3. script / style / nav / footer / header / aside などを除外する
4. main / article / body などから本文らしき部分を抽出する
5. UTF-8のテキストとして jms-docs/*.txt に保存する
```

生成された `.txt` ファイルはGit管理対象外です。

`.gitignore` では以下を除外しています。

```gitignore
jms-docs/*.txt
jms-html/
```

そのため、GitHubにはドキュメント本文はアップロードされません。

## 7. アプリケーション設定

設定ファイルは以下です。

```text
src/main/resources/application.yaml
```

主な設定例:

```yaml
server:
  host: "0.0.0.0"
  port: 8080

langchain4j:
  ollama:
    chat-model:
      enabled: true
      base-url: "http://localhost:11434"
      model-name: "llama3.2"

  rag:
    embedding-store-content-retriever:
      enabled: true
      max-results: 10
      min-score: 0.6
      embedding-model: "@default"
      embedding-store: "@default"

app:
  root: "jms-docs"
  inclusions: "*.txt"
```

重要な点:

```yaml
app:
  root: "jms-docs"
  inclusions: "*.txt"
```

この設定により、アプリケーションはリポジトリ直下の `jms-docs/` に生成された `.txt` ファイルをRAG対象として読み込みます。

## 8. ビルドする

```bash
mvn clean package
```

成功すると、以下のjarが作成されます。

```text
target/helidon-assistant.jar
```

## 9. アプリケーションを起動する

Ollamaが起動している状態で、以下を実行します。

```bash
java -jar target/helidon-assistant.jar
```

ブラウザで以下にアクセスします。

```text
http://localhost:8080/ui
```

## 10. 質問してみる

まずは以下を試してください。

```text
Oracle Java Management Serviceとは何ですか？
```

次に、以下のような質問も試せます。

```text
フリート管理とは何ですか？
Java Downloadとは何ですか？
管理対象インスタンスとは何ですか？
Javaランタイムとは何ですか？
Java Management Serviceでは何を管理できますか？
```

## 11. ポート競合エラーについて

すでにアプリが起動している状態でもう一度起動すると、以下のエラーが出ることがあります。

```text
java.net.BindException: Address already in use
```

これは `8080` ポートがすでに使われているという意味です。

使用中のプロセスを確認します。

```bash
lsof -i :8080
```

表示されたPIDを停止します。

```bash
kill <PID>
```

強制停止する場合:

```bash
kill -9 <PID>
```

または `application.yaml` のポートを変更します。

```yaml
server:
  port: 8081
```

その場合は以下にアクセスします。

```text
http://localhost:8081/ui
```

## 12. JMSとJava Message Serviceの違いについて

このPoCにおけるJMSは、Oracle Cloud Infrastructureの **Java Management Service** を指します。

一般的にJMSという略称は **Java Message Service** を意味することがありますが、本PoCでは対象外です。

そのため、AI ServiceのSystem Messageでは以下のように明示しています。

```text
In this application, JMS means Oracle Java Management Service,
not Java Message Service.
```

## 13. OpenAIではなくOllamaを使う理由

OpenAI APIを使う場合、APIキーと利用料金が必要です。

このPoCでは、より簡単に試せるように、無料でローカル実行できるOllamaを使っています。

メリット:

- OpenAI APIキー不要
- API課金なし
- ローカルPC上で実行可能
- PoCやデモに使いやすい
- ドキュメントRAGの仕組みを学習しやすい

注意点:

- 回答品質は利用するローカルモデルに依存します
- PCのCPU、メモリ、GPU性能に依存します
- 小さいモデルでは回答精度が十分でない場合があります
- 本番利用には、モデル選定、性能検証、セキュリティ設計が必要です

## 14. 主なファイル

このPoCで重要なファイルは以下です。

```text
fetch_docs.py
urls.txt
jms-docs/.gitkeep
pom.xml
src/main/resources/application.yaml
src/main/resources/WEB/index.html
src/main/java/net/dmitrykornilov/helidon/assistant/ai/ChatAiService.java
src/main/java/net/dmitrykornilov/helidon/assistant/rag/DocsIngestor.java
src/main/java/net/dmitrykornilov/helidon/assistant/rag/TextPreprocessor.java
```

### `fetch_docs.py`

Oracle DocsのHTMLを取得し、本文をテキスト化して `jms-docs/*.txt` に保存します。

### `urls.txt`

取得対象のOracle Docs URL一覧です。

### `TextPreprocessor.java`

`.txt` ファイルを読み込み、RAG投入用のチャンクに変換します。

### `ChatAiService.java`

Assistantの役割を定義します。  
このPoCでは、Oracle Java Management Service向けのAssistantとして設定しています。

### `application.yaml`

Ollama接続、RAG設定、ドキュメント配置場所を定義します。

### `index.html`

ブラウザUIです。画面タイトルを Java Management Service Assistant に変更しています。

## 15. ディレクトリ構成

代表的な構成は以下です。

```text
JMS_AI_Chatbox/
├── README.md
├── pom.xml
├── fetch_docs.py
├── urls.txt
├── jms-docs/
│   └── .gitkeep
├── src/
│   └── main/
│       ├── java/
│       └── resources/
└── target/
```

`jms-docs/*.txt` は、`python fetch_docs.py` 実行後にローカル環境で生成されます。

## 16. GitHub公開時の方針

このリポジトリでは、Oracle Docs本文をGitHubに含めません。

GitHubに含めるもの:

```text
fetch_docs.py
urls.txt
jms-docs/.gitkeep
アプリケーションコード
README.md
```

GitHubに含めないもの:

```text
jms-docs/*.txt
target/
.venv/
*.bak
.DS_Store
```

## 17. トラブルシューティング

### `jms-docs` にファイルが生成されない

以下を確認してください。

```bash
python fetch_docs.py
cat urls.txt
ls -l jms-docs
```

Pythonライブラリが足りない場合:

```bash
python -m pip install requests beautifulsoup4 lxml
```

### 日本語が文字化けする

`fetch_docs.py` では、HTMLをUTF-8として処理しています。

```python
BeautifulSoup(html_bytes, "lxml", from_encoding="utf-8")
```

古いスクリプトを使っている場合は、最新版の `fetch_docs.py` を使ってください。

### Ollamaにつながらない

確認:

```bash
curl http://localhost:11434/api/tags
```

起動:

```bash
ollama serve
```

### `llama3.2` が見つからない

```bash
ollama pull llama3.2
```

### `Address already in use` が出る

8080ポートが使用中です。

```bash
lsof -i :8080
kill <PID>
```

## 18. 免責事項

このプロジェクトはPoCおよび学習目的のサンプルです。

- 本番利用を前提としたものではありません
- 回答内容は必ず人間が確認してください
- Oracle Java Management Serviceの正式仕様や最新情報は、Oracle公式ドキュメントを確認してください
- Oracle Docs本文はリポジトリに同梱していません
- 利用者は `fetch_docs.py` により公式Oracle Docsからローカルにテキストを生成してください
- ローカルLLMの回答には誤りが含まれる可能性があります
- セキュリティ、認証、認可、監査、ログ管理などは本番要件に応じて別途実装が必要です

## 19. クレジット

このPoCは、以下の技術・ドキュメントを利用しています。

- Helidon
- LangChain4j
- Ollama
- Oracle Java Management Service Documentation

## 20. ライセンス

ライセンスは、利用するコード、依存ライブラリ、追加した実装の扱いに合わせて確認してください。

Oracle Docs由来のコンテンツについては、Oracle公式ドキュメントの利用条件を確認してください。
