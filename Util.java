import java.util.*;

public class Util{
    /* Regular one doesn't include leading 0's... */
    static String intToBinaryString(int toConvert){
        return String.format("%32s", Integer.toBinaryString(toConvert))
          .replace(' ', '0');
    }

    static String prettyBinaryMatch(int bitVector, int mask){
        char[] bitVectorChars = intToBinaryString(bitVector).toCharArray();
        char[] maskChars = intToBinaryString(mask).toCharArray();

        for(int i = 0; i < bitVectorChars.length; i++){
          if(maskChars[i] == '0')
            bitVectorChars[i] = 'x';
        }

        return new String(bitVectorChars);
    }

    static ArrayList listWithObject(Object obj) {
        ArrayList list = new ArrayList();
        list.add(obj);
        return list;
    }

    static boolean evaluateMatch(int value, int bitVector, int mask) {
        return (value & mask) == (bitVector & mask);
    }
}
