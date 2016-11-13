package dfs.extentservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

/**
 * Created by Dávid on 1.10.2016.
 */
public class ExtentServer implements dfs.extentservice.ExtentConnector, Serializable {
    private final String REGISTRY_NAME = "ExtentService";
    private static Registry registry;

    private final int mPort;
    private final String mPath;

    private HashMap<String, byte[]> fileSystem;

    public ExtentServer(int port, String path) throws RemoteException
    {
        mPort = port;
        mPath = path;
        fileSystem = new HashMap<>();

        exportAndBind();

        System.out.println("ExtentServer uses path " + path);
        new File(path).mkdir();
    }

    private void exportAndBind() throws RemoteException
    {
        System.out.println("Starting " + REGISTRY_NAME + " at port " + mPort);
        System.out.println("Retrieving registry");
        registry = LocateRegistry.createRegistry(mPort);

        System.out.println("Binding...");
        try {
            registry.bind(REGISTRY_NAME, this);
        } catch (AlreadyBoundException e) {
            System.out.println("Already bound");
            e.printStackTrace();
        }

        System.out.println("Object " + REGISTRY_NAME + " bound to registry at port " + mPort);
    }

    @Override
    public byte[] get(String path) throws RemoteException {
        System.out.print("ExtentServer: get: " + path + "\n");

        boolean isDirectory = path.endsWith("/");
        if(isDirectory)
        {
            File dir = new File(mPath + path);
            String[] dirContents = dir.list();
            if(dirContents == null)
                return null;

            String result = "";
            boolean isFirst = true;
            for(String name : dirContents)
            {
                if(!isFirst)
                    result += ",";
                result += name;
                if(new File(mPath + path + name).isDirectory())
                    result += "/";

                isFirst = false;
            }
            return result.getBytes();
        }
        else
        {
            try {
                return Files.readAllBytes(Paths.get(mPath + path));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private boolean isDirEmpty(String path)
    {
        /*String[] dirContents = new File(mPath + path).list();
        return dirContents.length == 0;*/
        int numFiles = 0;
        for(String val : fileSystem.keySet())
        {
            if(val.startsWith(path))
                numFiles++;

            if(numFiles > 1)
                return false;
        }

        return true;
    }

    @Override
    public boolean put(String path, byte[] content) throws RemoteException {
        System.out.println("ExtentServer: put: [" + path + "] " + (content == null ? "DELETE" : (String.valueOf(content.length) + "bytes")));
        boolean isDirectry = path.endsWith("/");
        boolean isEmpty = !isDirectry || isDirEmpty(path);

        if(fileSystem.containsKey(path))
        {
            if(content == null) {
                //Delete
                if(isEmpty) { //files are always empty
                    fileSystem.remove(path);
                    new File(mPath + path).delete();
                }
                else {
                    return false;
                }
            }
            else {
                //Replace
                fileSystem.put(path, content);
                if(isDirectry)
                {
                    return false;
                }
                else {
                    try {
                        Files.write(Paths.get(mPath + path), content);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }
        else if(content != null)
        {
            //Create
            fileSystem.put(path, content);
            if(isDirectry)
            {
                File dir = new File(mPath + path);
                return dir.mkdir();
            }
            else {
                try {
                    Files.write(Paths.get(mPath + path), content);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return true;
    }

    private void delete(File f) throws IOException {
        System.out.print("ExtentServer: delete\n");

        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Override
    public void stop() throws RemoteException {
        File dir = new File(mPath);
        try {
            delete(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            registry.unbind(REGISTRY_NAME);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
