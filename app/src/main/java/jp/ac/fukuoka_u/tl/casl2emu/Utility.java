package jp.ac.fukuoka_u.tl.casl2emu;

public class Utility {
    public Utility() {
    }

    /**
     * メモリ上のデータ3バイトで表現されている浮動小数点数をfloat型として読み込む
     *
     * @param c      符号を表す1バイトのデータ
     * @param a_kasu 数値を表す3バイトのデータ
     * @return float型の数値
     */
    public static double getFloatValue(char c, char[] a_kasu) {
        int[] _array = new int[7];
        int sign = 1;
        if ((a_kasu[0] >> 12) == 0xF) {
            sign = -1;
        }
        _array[0] = (a_kasu[0] & 0x0F00) >> 8;
        _array[1] = (a_kasu[0] & 0x00F0) >> 4;
        _array[2] = a_kasu[0] & 0x000F;
        _array[3] = (a_kasu[1] & 0xF000) >> 12;
        _array[4] = (a_kasu[1] & 0x0F00) >> 8;
        _array[5] = (a_kasu[1] & 0x00F0) >> 4;
        _array[6] = a_kasu[1] & 0x000F;
        short a_sisu = (short) c;
        double flt = (_array[0] * 0.1 + _array[1] * 0.01 + _array[2] * 0.001 + _array[3] * 0.0001 +
                _array[4] * 0.00001 + _array[5] * 0.000001 + _array[6] * 0.0000001);
        flt = Float.valueOf(String.format("%.7f", flt));
        return flt * sign * (Math.pow(10, a_sisu));
    }

    /**
     * float型で表現されている浮動小数点数を3バイトのデータに変換
     *
     * @param r float型の数値
     * @return 3バイト分のデータ
     */
    public static char[] getFloatArray(float r) {
        char kasu_sign = 0;
        char sisu_sign = 0;
        if (r < 0) {
            kasu_sign = 0xF;

            r = Math.abs(r);
        }
        int r_sisu = 0;

        float abs_r = Math.abs(r);
        if (abs_r < 0.1) {//絶対値が0.1以上1未満になるまで乗除を繰り返す
            for (short i = 0; i < 37; i++) {
                if (Math.abs(r) >= 0.1) {
                    r_sisu = (short) (i * -1);
                    break;
                } else {
                    r = r * 10;

                }
            }
        } else if (abs_r >= 1) {
            for (short i = 0; i < 37; i++) {
                if (Math.abs(r) < 1) {
                    r_sisu = i;
                    break;
                } else {
                    r = r / 10;
                }
            }
        }

        if (r_sisu < 0) {
            sisu_sign = 0x0F;
            r_sisu = Math.abs(r_sisu);
        }

        char[] r_array = new char[3];
        char[] _r = {48, 48, 48, 48, 48, 48, 48, 48, 48};
        char[] cs = String.valueOf(r).toCharArray();
        char[] _sisu = {48, 48, 48, 48};
        char[] cs_sisu = String.valueOf(r).toCharArray();

        int limit = (_r.length <= cs.length) ? _r.length : cs.length;
        for (int i = 0; i < limit; i++) {
            _r[i] = cs[i];
        }


        //'0'=48,'1'=49   '1'-48=1
        r_array[0] = (char) ((kasu_sign << 12) + ((_r[2] - 48) << 8) + ((_r[3] - 48) << 4) + ((_r[4] - 48)));
        r_array[1] = (char) (((_r[5] - 48) << 12) + ((_r[6] - 48) << 8) + ((_r[7] - 48) << 4) + ((_r[8] - 48)));
        r_array[2] = (char) ((sisu_sign << 12) + (((r_sisu % 1000) / 100) << 8) + (((r_sisu % 100) / 10) << 4) + (((r_sisu % 10))));
        return r_array;
    }
}