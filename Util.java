import java.util.*;

public class Util{
    /* Regular one doesn't include leading 0's... */
    static String intToBinaryString(int toConvert){
        return String.format("%32s", Integer.toBinaryString(toConvert))
          .replace(' ', '0');
    }

    static List<Integer> genMasks(int key, int flag) {
        char[] initVector = intToBinaryString(key).toCharArray();
        System.out.println(initVector);
        System.out.println(initVector.length);
        List<Integer> maskArray = new ArrayList<Integer>();
        char check = '0';
        char change = '1';

        if (flag == 1) {
          check = '1';
          change = '0';
        }

        for (int i = 1, j = 0; i < initVector.length; i++) {
          if (initVector[i] == check) {
            char[] mask = new char[31];
            Arrays.fill(mask, check);
            System.arraycopy(initVector, 1, mask, 0, i-1);
            mask[i-1] = change;
            String maskString = new String(mask);
            int n = Integer.parseInt(maskString, 2);
            if (flag == 1) {
              n = ~n;
            }
            maskArray.add(n);
          }
        }
        return maskArray;
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
