package com.wyq.utils;


import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class FileUtils {

    @SuppressWarnings("unused")
    public static File checkFile(String path) {
        File f = new File(path);
        if (null == f) {
            throw Lang.makeThrow("Fail to found file '%s'", path);
        }
        return f;
    }

    public static void write(File f, Object obj) {
        if (null == f || null == obj) {
            return;
        }
        if (f.isDirectory()) {
            throw Lang.makeThrow("Directory '%s' can not be write as File", f);
        }

        try {
            // 保证文件存在
            if (!f.exists()) {
                FileUtils.createNewFile(f);
            }
            // 输入流
            if (obj instanceof InputStream) {
                Streams.writeAndClose(Streams.fileOut(f), (InputStream) obj);
            }
            // 字节数组
            else if (obj instanceof byte[]) {
                Streams.writeAndClose(Streams.fileOut(f), (byte[]) obj);
            }
            // 文本输入流
            else if (obj instanceof Reader) {
                Streams.writeAndClose(Streams.fileOutw(f), (Reader) obj);
            }
            // 其他对象
            else {
                Streams.writeAndClose(Streams.fileOutw(f), obj.toString());
            }
        } catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    /**
     * 创建新文件，如果父目录不存在，也一并创建。可接受 null 参数
     *
     * @param f 文件对象
     * @return false，如果文件已存在。 true 创建成功
     * @throws IOException
     */
    public static boolean createNewFile(File f) throws IOException {
        if (null == f || f.exists()) {
            return false;
        }
        makeDir(f.getParentFile());
        return f.createNewFile();
    }

    /**
     * 创建新目录，如果父目录不存在，也一并创建。可接受 null 参数
     *
     * @param dir 目录对象
     * @return false，如果目录已存在。 true 创建成功
     * @throws IOException
     */
    public static boolean makeDir(File dir) {
        if (null == dir || dir.exists()) {
            return false;
        }
        return dir.mkdirs();
    }


    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path 文件路径
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path) {
        return findFile(path, ClassLoader.getSystemClassLoader(), Encoding.defaultEncoding());
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path        文件路径
     * @param klassLoader 参考 ClassLoader
     * @param enc         文件路径编码
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path, ClassLoader klassLoader, String enc) {
        path = absolute(path, klassLoader, enc);
        if (null == path) {
            return null;
        }
        return new File(path);
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path 文件路径
     * @param enc  文件路径编码
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path, String enc) {
        return findFile(path, ClassLoader.getSystemClassLoader(), enc);
    }

    /**
     * 从 CLASSPATH 下或从指定的本机器路径下寻找一个文件
     *
     * @param path        文件路径
     * @param klassLoader 使用该 ClassLoader进行查找
     * @return 文件对象，如果不存在，则为 null
     */
    public static File findFile(String path, ClassLoader klassLoader) {
        return findFile(path, klassLoader, Encoding.defaultEncoding());
    }

    /**
     * 获取一个路径的绝对路径。如果该路径不存在，则返回null
     *
     * @param path 路径
     * @return 绝对路径
     */
    public static String absolute(String path) {
        return absolute(path, ClassLoader.getSystemClassLoader(), Encoding.defaultEncoding());
    }

    /**
     * 获取一个路径的绝对路径。如果该路径不存在，则返回null
     *
     * @param path        路径
     * @param klassLoader 参考 ClassLoader
     * @param enc         路径编码方式
     * @return 绝对路径
     */
    public static String absolute(String path, ClassLoader klassLoader, String enc) {
        path = normalize(path, enc);
        if (StringVerification.isEmpty(path)) {
            return null;
        }
        File f = new File(path);
        if (!f.exists()) {
            URL url = null;
            try {
                url = klassLoader.getResource(path);
                if (null == url) {
                    url = Thread.currentThread().getContextClassLoader().getResource(path);
                }
                if (null == url) {
                    url = ClassLoader.getSystemResource(path);
                }
            } catch (Throwable e) {
            }
            if (null != url) {
                // 通过URL获取String,一律使用UTF-8编码进行解码
                return normalize(url.getPath(), Encoding.UTF8);
            }
            return null;
        }
        return path;
    }

    /**
     * 让路径变成正常路径，将 ~ 替换成用户主目录
     *
     * @param path 路径
     * @return 正常化后的路径
     */
    public static String normalize(String path) {
        return normalize(path, Encoding.defaultEncoding());
    }

    /**
     * 让路径变成正常路径，将 ~ 替换成用户主目录
     *
     * @param path 路径
     * @param enc  路径编码方式
     * @return 正常化后的路径
     */
    public static String normalize(String path, String enc) {
        if (StringVerification.isEmpty(path)) {
            return null;
        }
        if (path.charAt(0) == '~') {
            path = home() + path.substring(1);
        }
        try {
            return URLDecoder.decode(path, enc);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * @return 当前账户的主目录全路径
     */
    public static String home() {
        return System.getProperty("user.home");
    }

    /**
     * 获取文件主名。 即去掉后缀的名称
     *
     * @param path 文件路径
     * @return 文件主名
     */
    public static String getMajorName(String path) {
        int len = path.length();
        int l = 0;
        int r = len;
        for (int i = r - 1; i > 0; i--) {
            if (r == len) {
                if (path.charAt(i) == '.') {
                    r = i;
                }
            }
            if (path.charAt(i) == '/' || path.charAt(i) == '\\') {
                l = i + 1;
                break;
            }
        }
        return path.substring(l, r);
    }

    /**
     * 获取文件主名。 即去掉后缀的名称
     *
     * @param f 文件
     * @return 文件主名
     */
    public static String getMajorName(File f) {
        return getMajorName(f.getAbsolutePath());
    }

    /**
     * @see #getSuffixName(String)
     */
    public static String getSuffixName(File f) {
        if (null == f) {
            return null;
        }
        return getSuffixName(f.getAbsolutePath());
    }

    /**
     * 获取文件后缀名，不包括 '.'，如 'abc.gif','，则返回 'gif'
     *
     * @param path 文件路径
     * @return 文件后缀名
     */
    public static String getSuffixName(String path) {
        if (null == path) {
            return null;
        }
        int p0 = path.lastIndexOf('.');
        int p1 = path.lastIndexOf('/');
        if (-1 == p0 || p0 < p1) {
            return "";
        }
        return path.substring(p0 + 1);
    }

    /**
     * @see #getSuffix(String)
     */
    public static String getSuffix(File f) {
        if (null == f) {
            return null;
        }
        return getSuffix(f.getAbsolutePath());
    }

    /**
     * 获取文件后缀名，包括 '.'，如 'abc.gif','，则返回 '.gif'
     *
     * @param path 文件路径
     * @return 文件后缀
     */
    public static String getSuffix(String path) {
        if (null == path) {
            return null;
        }
        int p0 = path.lastIndexOf('.');
        int p1 = path.lastIndexOf('/');
        if (-1 == p0 || p0 < p1) {
            return "";
        }
        return path.substring(p0);
    }

    /***
     *
     * @param fileByte
     * @param fileName
     * @param extend
     * @return
     */
    public static String createFile(byte[] fileByte, String dataPth, String fileName, String extend) {
        String filePath = checkPath(dataPth, fileName, extend);

        File file1 = new File(filePath);
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file1));
            out.write(fileByte);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    /***
     * 创建目录
     * @param name
     * @param extend
     * @return
     */
    private static String checkPath(String dataPath, String name, String extend) {
        File upload = new File(dataPath);
        if (!upload.exists()) {
            upload.mkdirs();
        }
        String absolutePath = upload.getAbsolutePath();
        String filepath = absolutePath + "/" + name + "." + extend;
        return filepath;
    }

    /***
     * 删除文件
     * @param filePath
     */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        file.deleteOnExit();
    }

    /**
     * 读取图片文件并获取base64
     */
    public static String readPicBase64(String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
