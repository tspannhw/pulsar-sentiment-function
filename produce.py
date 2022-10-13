import pulsar

client = pulsar.Client('pulsar://pulsar1:6650')
# //nvidia-desktop:6650')

producer = client.create_producer('persistent://public/default/chat2')

for i in range(10):
    producer.send(('This is horrible, it makes me angry, what is everything bad and wrong.%d' % i).encode('utf-8'))

client.close()
