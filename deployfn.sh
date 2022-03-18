source source.sh

bin/pulsar-admin functions delete --name SentimentAnalysis --namespace default --tenant public

bin/pulsar-admin functions create --auto-ack true --jar sentiment-1.0.jar --classname "dev.pulsarfunction.sentiment.SentimentFunction" --dead-letter-topic "persistent://public/default/chatdead" --inputs "persistent://public/default/chat" --log-topic "persistent://public/default/chatlog" --name SentimentAnalysis --namespace default --output "persistent://public/default/chatresult" --tenant public --max-message-retries 5
