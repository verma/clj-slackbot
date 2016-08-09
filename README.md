[![CircleCI](https://circleci.com/gh/kliph/mog-clj-slackbot.svg?style=svg)](https://circleci.com/gh/kliph/mog-clj-slackbot)

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

# Mog: a clj-slackbot fork

A clojure project to run a eval bot which can be easily hooked up in Slack.

## How to run?

__Please note that configuration stuff has now changed.  Also, things are relatively newer and untested. This will soon be fixed.  Right now is probably not a good time to upgrade to the newer stuff.__

You need to create a `config.edn` file based on the `config.example.edn` file available in the repository.  This file needs to be in the current path.  The program just looks for `config.edn` in the current directory.

This bot now supports different kinds of comm-links (communication channels to talk to slack).  You can either use the older webhook configuration, or you can use the newer RTM API from Slack to setup a bot.

### Using the Webhook configuration
Webhook configuration needs four things defined in `config.edn`:

- `:comm :mog.comms.slack-web-hook/start`
- `:post-url` - The post URL to post responses to a channel.
- `:command-token` - The token you get when you create a slash command in slack. Usually something like `/clj`.
- `:port` - The port to run the web-server on.

Webhook works by listening to _slash command_ requests and then responding to them using a channel post.

Once you have the program running with this comm-link, it will start a web server on the specified port and listen for requests on `/clj` end-point.  It is made to accept slack command (with clojure to evaluate in the "text" field).  The evaluated result is sent out to the POST url on the same channel on which it was received.

Please see the section on [Incoming Webhooks](https://api.slack.com/incoming-webhooks) and [Slash Commands](https://api.slack.com/slash-commands).

### Using the RTM API Bot
__WARNING: New Stuff__

You can create a bot in slack which should give you an `api-token`.  Please see [this](https://api.slack.com/rtm) for more details. You need the following configuration to setup an RTM API:

- `:comm :mog.comms.slack-rtm/start-event-loop`
- `:api-token` - The bot token you'll get once you create a slack bot.
- `:prefix` - The prefix which triggers eval. E.g. you could set it to a `,` and then messages to the channel like `,(+ 1 1)` will be evaluated by the bot.

Please see the section on [Bot Users](https://api.slack.com/bot-users).

### Running the bot
Copy the `.java.policy` file to your home directory.

If you have the jar file, just do:

    java -jar mog.jar

Or can checkout the source and run:

    lein run

If your configuration file is not in the current directory of the program, you can specify a `CONFIG_FILE` environment variable pointing to the location of the configuration file.

    CONFIG_FILE=/path/to/config.edn java -jar mog.jar

## License

Copyright © 2014 Uday Verma.  Licensed under the same terms as Clojure (EPL).

Mog Copyright © 2016 Cliff Rodgers.  Licensed under Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php).
