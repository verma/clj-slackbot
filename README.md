# clj-slackbot

A clojure project to run a eval bot which can be easily hooked up in Slack.

## How to run?

You need:
 - *POST_URL* - The URL to post stuff to slack (an incoming web-hook).
 - *COMMAND_TOKEN* - The token for your slash command hook.

If you have the jar file, just do:

    POST_URL=<post url> COMMAND_TOKEN=<command token> java -jar clj-slackbot.jar

Or can checkout the source and run:

    POST_URL=<post url> COMMAND_TOKEN=<command token> lein run


Once you have the server running it will listen for requests on `/clj` end-point.  It is made to accept slack command (with clojure to evaluate in the "text" field).  The evaluated result is sent out to the POST url on the same channel on which it was received.

## Slack Configuration
Create two integrations:

 - Slash Command - Make it post to wherever your server is running: http://myhost.com/clj and note down the COMMAND_TOKEN.
 - Incoming Webhook - Create a new Incoming Webhook, notedown its POST_URL.

## License

Copyright © 2014 Uday Verma.  Licensed under the same terms as Clojure (EPL).
