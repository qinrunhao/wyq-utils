package com.wyq.utils;

import java.io.*;
import java.nio.charset.Charset;

public class Streams {
	
	 private static final int BUF_SIZE = 8192;
	
	public static void write(Writer writer, CharSequence cs) throws IOException {
        if (null != cs && null != writer) {
            writer.write(cs.toString());
            writer.flush();
        }
    }
	
	/**
     * 将输入流写入一个输出流。块大小为 8192
     * <p>
     * <b style=color:red>注意</b>，它并不会关闭输入/出流
     * 
     * @param ops
     *            输出流
     * @param ins
     *            输入流
     * 
     * @return 写入的字节数
     * @throws IOException
     */
    public static long write(OutputStream ops, InputStream ins) throws IOException {
        return write(ops, ins, BUF_SIZE);
    }
    
    /**
     * 将输入流写入一个输出流。
     * <p>
     * <b style=color:red>注意</b>，它并不会关闭输入/出流
     * 
     * @param ops
     *            输出流
     * @param ins
     *            输入流
     * @param bufferSize
     *            缓冲块大小
     * 
     * @return 写入的字节数
     * 
     * @throws IOException
     */
    public static long write(OutputStream ops, InputStream ins, int bufferSize) throws IOException {
        if (null == ops || null == ins) {
            return 0;
        }

        byte[] buf = new byte[bufferSize];
        int len;
        long bytesCount = 0;
        while (-1 != (len = ins.read(buf))) {
            bytesCount += len;
            ops.write(buf, 0, len);
        }
        // 啥都没写，强制触发一下写
        // 这是考虑到 walnut 的输出流实现，比如你写一个空文件
        // 那么输入流就是空的，但是 walnut 的包裹输出流并不知道你写过了
        // 它人你就是打开一个输出流，然后再关上，所以自然不会对内容做改动
        // 所以这里触发一个写，它就知道，喔你要写个空喔。
        if (0 == bytesCount) {
            ops.write(buf, 0, 0);
        }
        ops.flush();
        return bytesCount;
    }
	
	  /**
     * 根据一个文件路径建立一个输出流
     * 
     * @param path
     *            文件路径
     * @return 输出流
     */
    public static OutputStream fileOut(String path) {
        return fileOut(FileUtils.findFile(path));
    }

    /**
     * 根据一个文件建立一个输出流
     * 
     * @param file
     *            文件
     * @return 输出流
     */
    public static OutputStream fileOut(File file) {
        try {
            return buff(new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
    }
    
    /**
     * 为一个输出流包裹一个缓冲流。如果这个输出流本身就是缓冲流，则直接返回
     * 
     * @param ops
     *            输出流。
     * @return 缓冲输出流
     */
    public static BufferedOutputStream buff(OutputStream ops) {
        if (ops == null) {
            throw new NullPointerException("ops is null!");
        }
        if (ops instanceof BufferedOutputStream) {
            return (BufferedOutputStream) ops;
        }
        return new BufferedOutputStream(ops);
    }
    
    /**
     * 为一个输入流包裹一个缓冲流。如果这个输入流本身就是缓冲流，则直接返回
     * 
     * @param ins
     *            输入流。
     * @return 缓冲输入流
     */
    public static BufferedInputStream buff(InputStream ins) {
        if (ins == null) {
            throw new NullPointerException("ins is null!");
        }
        if (ins instanceof BufferedInputStream) {
            return (BufferedInputStream) ins;
        }
        // BufferedInputStream的构造方法,竟然是允许null参数的!! 我&$#^$&%
        return new BufferedInputStream(ins);
    }
    
    
    /**
     * 根据一个文件路径建立一个 UTF-8 文本输出流
     * 
     * @param path
     *            文件路径
     * @return 文本输出流
     */
    public static Writer fileOutw(String path) {
        return fileOutw(FileUtils.findFile(path));
    }

    /**
     * 根据一个文件建立一个 UTF-8 文本输出流
     * 
     * @param file
     *            文件
     * @return 输出流
     */
    public static Writer fileOutw(File file) {
        return utf8w(fileOut(file));
    }
    
    public static Writer utf8w(OutputStream os) {
        return new OutputStreamWriter(os, Charset.forName("UTF-8"));
    }
    
    public static long writeAndClose(OutputStream ops, InputStream ins, int buf) {
        try {
            return write(ops, ins, buf);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            safeClose(ops);
            safeClose(ins);
        }
    }
    
    /**
     * 关闭一个可关闭对象，可以接受 null。如果成功关闭，返回 true，发生异常 返回 false
     * 
     * @param cb
     *            可关闭对象
     * @return 是否成功关闭
     */
    public static boolean safeClose(Closeable cb) {
        if (null != cb) {
            try {
                cb.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将文本输入流写入一个文本输出流。块大小为 8192
     * <p>
     * <b style=color:red>注意</b>，它会关闭输入/出流
     * 
     * @param writer
     *            输出流
     * @param reader
     *            输入流
     */
    public static long writeAndClose(Writer writer, Reader reader) {
        try {
            return write(writer, reader);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            safeClose(writer);
            safeClose(reader);
        }
    }

    /**
     * 将文本输入流写入一个文本输出流。块大小为 8192
     * <p>
     * <b style=color:red>注意</b>，它并不会关闭输入/出流
     * 
     * @param writer
     *            输出流
     * @param reader
     *            输入流
     * @throws IOException
     */
    public static long write(Writer writer, Reader reader) throws IOException {
        if (null == writer || null == reader) {
            return 0;
        }

        char[] cbuf = new char[BUF_SIZE];
        int len, count = 0;
        while (true) {
            len = reader.read(cbuf);
            if (len == -1) {
                break;
            }
            writer.write(cbuf, 0, len);
            count += len;
        }
        return count;
    }
    
    /**
     * 将一段文本全部写入一个writer。
     * <p>
     * <b style=color:red>注意</b>，它会关闭输出流
     * 
     * @param writer
     *            输出流
     * @param cs
     *            文本
     */
    public static void writeAndClose(Writer writer, CharSequence cs) {
        try {
            write(writer, cs);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            safeClose(writer);
        }
    }
    
    /**
     * 将一个字节数组写入一个输出流。
     * <p>
     * <b style=color:red>注意</b>，它会关闭输出流
     * 
     * @param ops
     *            输出流
     * @param bytes
     *            字节数组
     */
    public static void writeAndClose(OutputStream ops, byte[] bytes) {
        try {
            write(ops, bytes);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            safeClose(ops);
        }
    }
    
    /**
     * 将一个字节数组写入一个输出流。
     * <p>
     * <b style=color:red>注意</b>，它并不会关闭输出流
     * 
     * @param ops
     *            输出流
     * @param bytes
     *            字节数组
     * @throws IOException
     */
    public static void write(OutputStream ops, byte[] bytes) throws IOException {
        if (null == ops || null == bytes || bytes.length == 0) {
            return;
        }
        ops.write(bytes);
    }
    
    /**
     * 将输入流写入一个输出流。块大小为 8192
     * <p>
     * <b style=color:red>注意</b>，它会关闭输入/出流
     * 
     * @param ops
     *            输出流
     * @param ins
     *            输入流
     * @return 写入的字节数
     */
    public static long writeAndClose(OutputStream ops, InputStream ins) {
        try {
            return write(ops, ins);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            safeClose(ops);
            safeClose(ins);
        }
    }

}
