/**
 * @Copyright cn.edu.fudan.iipl
 */

package cn.edu.fudan.iipl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * a useful util class for file or directory exist judging and creating
 *
 * @author Yong Chen
 * @since 2015-01-28
 */
public class FileUtil {

    /**
     * judge a file specified by the parameter "filePath" exist or not
     *
     * @param filePath
     * @return true if it is a file and already exist, false for not a file or not exist.
     */
    public static boolean fileExistJudge(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            return true;
        return false;
    }

    /**
     * <pre>
     * judge a file specified by the parameter "filePath" exist
     *  or not, System.exit if file not exist
     * </pre>
     *
     * @param filePath
     */
    public static void fileJudge(String filePath) {
        if (!fileExistJudge(filePath)) {
            System.err.println(filePath + " not exist!");
            System.exit(1);
        }
    }

    /**
     * judge a directory specified by the parameter "dirPath" exist or not
     *
     * @param dirPath
     * @return true if it is a directory and already exist, false for not a directory or not exist.
     */
    public static boolean dirExistJudge(String dirPath) {
        File dir = new File(dirPath);
        if (dir.isDirectory()) {
            if (dir.exists())
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    /**
     * judge a directory specified by the parameter "dirPath" exist or not, System.exit if dir not
     * exist
     *
     * @param dirPath
     */
    public static void dirJudge(String dirPath) {
        if (!dirExistJudge(dirPath)) {
            System.err.println(dirPath + " not exist!");
            System.exit(1);
        }
    }

    /**
     * create the file specified by parameter "filePath"
     *
     * @param filePath
     * @return path of the file if it was created success, or already exist and it is a file; be
     *         aborted if creating failed.
     */
    public static String fileCreate(String filePath) {
        filePath = getCanonicalPath(filePath);
        try {
            File file = new File(filePath);
            if (file.exists())
                if (file.isFile()) {
                    System.out.println("file " + filePath + " already exist!");
                    return filePath;
                } else {
                    System.err.println("exist! And " + filePath + "is not a file!");
                    System.exit(1);
                }
            else {
                if (file.createNewFile()) {
                    System.out.println("create file " + filePath + " success!");
                    return filePath;
                } else {
                    System.err.println("create file " + filePath + " failed!");
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * create the directory specified by the parameter "dirPath"
     *
     * @param dirPath
     * @return path of the directory if it was created success, or already exist and it is a
     *         directory; be aborted if creating failed.
     */
    public static String dirCreate(String dirPath) {
        dirPath = getCanonicalPath(dirPath);
        File dir = new File(dirPath);
        if (dir.exists())
            if (dir.isDirectory()) {
                System.out.println("dir " + dirPath + " already exist!");
                return dirPath;
            } else {
                System.err.println("exist! And " + dirPath + "is not a directory!");
                System.exit(1);
            }
        else {
            if (dir.mkdir()) {
                System.out.println("create dir " + dirPath + " success!");
                return dirPath;
            } else {
                System.err.println("create directory " + dirPath + " failed!");
                System.exit(1);
            }
        }
        return null;
    }

    /**
     * create the directorys specified by the parameter "dirPath"
     *
     * @param dirPath
     * @return path of the directory if it was created success, or already exist and it is a
     *         directory; be aborted if creating failed.
     */
    public static String dirsCreate(String dirPath) {
        dirPath = getCanonicalPath(dirPath);
        File dir = new File(dirPath);
        if (dir.exists())
            if (dir.isDirectory()) {
                System.out.println("dir " + dirPath + " already exist!");
                return dirPath;
            } else {
                System.err.println("exist! And " + dirPath + "is not a directory!");
                System.exit(1);
            }
        else {
            if (dir.mkdirs()) {
                System.out.println("create dir " + dirPath + " success!");
                return dirPath;
            } else {
                System.err.println("create directory " + dirPath + " failed!");
                System.exit(1);
            }
        }
        return null;
    }

    /**
     * get canonical path of the file path specified by the parameter "path"
     *
     * @param path
     * @return canonicalPath if getting success, null for failed
     * @see FileUtil#getCanonicalPath(File)
     */
    public static String getCanonicalPath(String path) {
        String canonicalPath = null;
        try {
            File file = new File(path);
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return canonicalPath;
    }

    /**
     * get canonical path of the file path specified by the parameter "file"
     *
     * @param file
     * @return canonicalPath if getting success, null for failed
     * @see FileUtil#getCanonicalPath(String)
     */
    public static String getCanonicalPath(File file) {
        String canonicalPath = null;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return canonicalPath;
    }

    /**
     * <pre>
     * copy file from src to obj. both src and obj are file paths.
     * src must exist and obj must not exist! otherwise system will exit immediately.
     */
    public static void copyFile(String src, String obj) {
        fileJudge(src);
        if (new File(obj).exists()) {
            System.out.println(obj + " already exist! copying failed!");
            System.exit(1);
        }

        fileCreate(obj);

        BufferedReader bReader = null;
        FileWriter fWriter = null;
        try {
            bReader = new BufferedReader(new FileReader(src));
            fWriter = new FileWriter(obj);
            String tempString = null;
            while ((tempString = bReader.readLine()) != null) {
                fWriter.write(tempString + "\n", 0, (tempString + "\n").length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fWriter.close();
                bReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFileName(String path) {
        return new File(getCanonicalPath(path)).getName();
    }
}
