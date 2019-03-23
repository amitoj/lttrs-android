/*
 * Copyright 2019 Daniel Gultsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rs.ltt.android.util;

import android.graphics.Color;

import org.hsluv.HUSLColorConverter;

import java.security.MessageDigest;

public class XEP0392Helper {

    private static double angle(String nickname) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(nickname.getBytes("UTF-8"));
            int angle = ((int) (digest[0]) & 0xff) + ((int) (digest[1]) & 0xff) * 256;
            return angle / 65536.;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static int rgbFromKey(String key) {
        double[] hsluv = new double[3];
        hsluv[0] = angle(key) * 360;
        hsluv[1] = 100;
        hsluv[2] = 50;
        double[] rgb = HUSLColorConverter.hsluvToRgb(hsluv);
        return Color.rgb((int) Math.round(rgb[0] * 255), (int) Math.round(rgb[1] * 255), (int) Math.round(rgb[2] * 255));
    }
}
