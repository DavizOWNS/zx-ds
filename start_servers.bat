start cmd /c java -jar out/artifacts/jar/dfs-server.jar 6500
timeout 1
start cmd /c java -jar out/artifacts/jar/dfs-server.jar 6501 6500
timeout 3
start cmd /c java -jar out/artifacts/jar/lockcache-client-test.jar 6501
exit