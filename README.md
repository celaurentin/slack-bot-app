## Slack Bot App in Scala 3

This Slack App use [Slack Socket Mode](https://api.slack.com/) in [Scala 3](https://dotty.epfl.ch/)!

* [Intro to Socket Mode](https://api.slack.com/apis/connections/socket)
* [Getting Started with Slack Java SDK](https://slack.dev/java-slack-sdk/guides/getting-started-with-bolt-socket-mode)
* [Socket Mode in Slack Java SDK](https://slack.dev/java-slack-sdk/guides/socket-mode)

### Usage

```bash
brew install sbt
export SLACK_BOT_TOKEN=<your_token> 
export SLACK_APP_TOKEN=<your_token>
sbt run
```

Slack's command:
```/TELLME```
