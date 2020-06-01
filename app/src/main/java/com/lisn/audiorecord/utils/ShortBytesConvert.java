package com.lisn.audiorecord.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author : lishan
 * @e-mail : cnlishan@163.com
 * @date : 2020/5/27 3:59 PM
 * @desc :
 */
public class ShortBytesConvert {
    /**
     * 注意：代码里有大小端参数，小端 LITTLE_ENDIAN   大端BIG_ENDIAN，
     * 简单来说，大端模式就是存储器的高地址存放低字节；
     * 小端模式就是存储器的低地址存放低字节。
     * 我们常用的X86结构是小端模式，而KEIL C51则为大端模式。
     * 很多的ARM，DSP都为小端模式。有些ARM处理器还可以由硬件来选择是大端模式还是小端模式。
     * 由于这里是在X86机器上运行，所以选择小端。
     *
     */
    public static short[] bytesToShort(byte[] bytes) {
        if(bytes==null){
            return null;
        }
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
    public static byte[] shortToBytes(short[] shorts) {
        if(shorts==null){
            return null;
        }
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);

        return bytes;
    }

    public static void main(String[] args) {
        byte[] ba = {21,32,45,98,46,85};
        short[] sa = bytesToShort(ba);
        byte[] bb = shortToBytes(sa);

        System.out.println("ba=" + Arrays.toString(ba) + ",sa=" + Arrays.toString(sa) + ",bb=" + Arrays.toString(bb));
    }
}
