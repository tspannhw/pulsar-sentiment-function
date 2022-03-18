import pulsar

client = pulsar.Client('pulsar://127.0.0.1:6650')
# //nvidia-desktop:6650')

producer = client.create_producer('persistent://public/default/chat')

for i in range(10):
    producer.send(('This is horrible, it makes me angry, what is everything bad and wrong.%d' % i).encode('utf-8'))

client.close()
