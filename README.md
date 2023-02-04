# web-tree

Open source example of web crawler, user puts a URL, program 
goes to filled URL and extract links to another URLs from an HTML page,
and again, and again. All conclusions save to an OpenSearch. That is 
needed to make search on data.

<img src="https://user-images.githubusercontent.com/42806772/216792496-866ee9c0-7464-45c6-b8a8-ab6fddc4edbe.png" width="600" height="300">

Example of `docker-compose.yml` for OpenSearch 
(if you run it on VM, set `sysctl -w vm.max_map_count=262144` in a `/var/lib/boot2docker/profile`, 
problem https://github.com/boot2docker/boot2docker/issues/1216): 

```yaml
version: '3'
services:
  opensearch-node-one:
    image: opensearchproject/opensearch:latest
    container_name: opensearch-node-one
    environment:
      - cluster.name=opensearch-cluster # Name the cluster
      - node.name=opensearch-node-one # Name the node that will run in this container
      - discovery.seed_hosts=opensearch-node-one,opensearch-node-two # Nodes to look for when discovering the cluster
      - cluster.initial_cluster_manager_nodes=opensearch-node-one,opensearch-node-two # Nodes eligibile to serve as cluster manager
      - bootstrap.memory_lock=true # Disable JVM heap memory swapping
      - "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m" # Set min and max JVM heap sizes to at least 50% of system RAM
    ulimits:
      memlock:
        soft: -1 # Set memlock to unlimited (no soft or hard limit)
        hard: -1
      nofile:
        soft: 65536 # Maximum number of open files for the opensearch user - set to at least 65536
        hard: 65536
    volumes:
      - opensearch-data1:/usr/share/opensearch/data # Creates volume called opensearch-data1 and mounts it to the container
    ports:
      - "9200:9200" # REST API
      - "9600:9600" # Performance Analyzer
    networks:
      - opensearch-net
  opensearch-node-two:
    image: opensearchproject/opensearch:latest
    container_name: opensearch-node-two
    environment:
      - cluster.name=opensearch-cluster
      - node.name=opensearch-node-two
      - discovery.seed_hosts=opensearch-node-one,opensearch-node-two
      - cluster.initial_cluster_manager_nodes=opensearch-node-one,opensearch-node-two
      - bootstrap.memory_lock=true
      - "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m"
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    volumes:
      - opensearch-data2:/usr/share/opensearch/data
    networks:
      - opensearch-net
  opensearch-dashboard:
    image: opensearchproject/opensearch-dashboards:latest
    container_name: opensearch-dashboard
    ports:
      - "5601:5601"
    expose:
      - "5601"
    environment:
      OPENSEARCH_HOSTS: '["https://opensearch-node-one:9200","https://opensearch-node-two:9200"]'
    networks:
      - opensearch-net

volumes:
  opensearch-data1:
  opensearch-data2:

networks:
  opensearch-net:
```

Configuration example:

```properties
open-search-remote=192.168.99.100:9200
open-search-index=internet
open-search-login=admin
open-search-password=admin
crawler-root-page=https://www.google.com
crawler-thread-size=40
crawler-connection-timeout=2000
crawler-read-timeout=2000
crawler-reties-count=10
disable-certificate-trust=true
```
