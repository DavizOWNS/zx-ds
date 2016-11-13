package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by DAVID on 13.11.2016.
 */
public class ExtentCacheImpl implements ExtentCache, Serializable {

    private String ID = UUID.randomUUID().toString();
    private ExtentConnector extentServer;

    private Dictionary<String, FileEntry> files;

    public ExtentCacheImpl(ExtentConnector extentConnector)
    {
        extentServer = extentConnector;
        files = new Hashtable<>();
    }

    @Override
    public byte[] get(String path){
        System.out.print("ExtentCache[" + ID + "]: get: " + path + "\n");

        boolean isDir = path.endsWith("/");
        if(isDir)
        {
            Enumeration<String> paths = files.keys();
            int pathLen = path.length();

            List<String> names = new ArrayList<>();
            while(paths.hasMoreElements())
            {
                String current = paths.nextElement();
                if(current.startsWith(path) && current.length() > pathLen)
                {
                    String subPath = current.substring(pathLen);
                    int idx = subPath.indexOf('/');
                    if(idx >= 0)
                    {
                        subPath = subPath.substring(0, idx);
                    }
                    if(!names.contains(subPath))
                        names.add(subPath);
                }
            }

            String result = "";
            boolean isFirst = true;
            for (String name : names)
            {
                if(!isFirst)
                    result += ",";

                result += name;

                isFirst = false;
            }
            return result.getBytes();
        }

        FileEntry result = files.get(path);
        if(result == null)
        {
            try {
                result = new FileEntry(path);
                result.contents = extentServer.get(path);
                result.isDirty = false;

                files.put(path, result);
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }

            files.put(path, result);
        }

        return result.contents;
    }

    @Override
    public boolean put(String path, byte[] bytes) {
        System.out.print("ExtentCache[" + ID + "]: put: " + path + "[" + (bytes == null ? "<null>" : bytes.length) + "]" + "\n");

        FileEntry entry = files.get(path);
        if(entry == null)
        {
            entry = new FileEntry(path);
            entry.contents = bytes;
            entry.isDirty = true;

            files.put(path, entry);
        }
        else
        {
            entry.contents = bytes;
            entry.isDirty = true;
        }


        return true;
    }

    @Override
    public void update(String path) {
        System.out.print("ExtentCache[" + ID + "]: update: " + path + "\n");

        FileEntry current = files.get(path);

        if(current.isDirty)
        {
            try {
                System.out.println("put in " + current.getPath() + (current.contents == null ? "<null>" : (String.valueOf(current.contents.length) + "bytes")));
                boolean result = extentServer.put(current.getPath(), current.contents);
                current.isDirty = false;
                System.out.println("Result: " + String.valueOf(result));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void flush(String path) {
        System.out.print("ExtentCache[" + ID + "]: flush: " + path + "\n");

    }

    private class FileEntry
    {
        private String path;

        public byte[] contents;
        public boolean isDirty;

        public FileEntry(String path)
        {
            this.path = path;
        }

        public String getPath() {return path;}
    }
}
