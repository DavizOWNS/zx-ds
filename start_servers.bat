start cmd /c java -jar out/artifacts/jar/dfs-server.jar 6500
timeout 1
start cmd /c java -jar out/artifacts/jar/dfs-server.jar 6501 6500
start cmd /c java -jar out/artifacts/jar/dfs-server.jar 6502 6500
exit