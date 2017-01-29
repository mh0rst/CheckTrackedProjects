/*
 * CheckTrackedProjects - An Android tracked AOSP project checker.  
 * Copyright (C) 2017 Moritz Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mh0rst.android.checktrackedprojects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Fetcher {

    private static final String BASE_URL = "https://android.googlesource.com";

    public static String fetchFromURL(String url) throws IOException {
        URLConnection conn;
        if (url.startsWith("/")) {
            conn = new URL(BASE_URL + url).openConnection();
        } else {
            conn = new URL(url).openConnection();
        }
        ((HttpURLConnection) conn).setRequestProperty("Accept-Encoding", "gzip");
        InputStream stream = null;
        if ("gzip".equals(conn.getContentEncoding())) {
            stream = new GZIPInputStream(conn.getInputStream());
        } else {
            stream = conn.getInputStream();
        }
        return new String(readAllBytes(stream), StandardCharsets.UTF_8);
    }

    private static byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

    public static String findLink(String haystack, String inUrl) {
        Pattern pattern = Pattern.compile("<a href=\"(.*" + Pattern.quote(inUrl) + ".*?)\"");
        Matcher matcher = pattern.matcher(haystack);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
