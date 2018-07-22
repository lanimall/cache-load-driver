# Ehcache 3 Provider

## Running this provider

### Start the Terracotta Server

[Download](https://github.com/ehcache/ehcache3/releases) the full Ehcache clustering kit, if you have not.  This kit contains the Terracotta Server, which enables distributed caching with Ehcache.

Open a terminal and change into the directory where you have this sample.

Start the Terracotta Server, using the configuration supplied with this sample:

```bash
$path_to_ehcache_clustered_kit/server/bin/start-tc-server.sh -f ./configs/tc-config.xml
```

(For Windows environments, use the .bat start script rather than the .sh one).

Wait a few seconds for the Terracotta Server to start up - there will be a clear message in the terminal stating the server is *ACTIVE* and *ready for work*.

### Start the Terracotta Server in High availability

If you want to use high availability, you need to launch two Terracotta Server, one active, one passive.

In case of failure of the active server, the passive will take over automatically.
You can of course have as many passives as you like.
Here is an example with two servers:

(Launch these two commands from two separated shells)

Shell 1:

```bash
$path_to_ehcache_clustered_kit/server/bin/start-tc-server.sh -f ./configstc-config-ha.xml -n clustered1
```

Shell 2:

```bash
$path_to_ehcache_clustered_kit/server/bin/start-tc-server.sh -f ./configstc-config-ha.xml -n clustered2
```

### Run the application

