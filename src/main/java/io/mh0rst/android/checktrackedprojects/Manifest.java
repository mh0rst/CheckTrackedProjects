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

import static io.mh0rst.android.checktrackedprojects.Manifest.ProjectMap.entry;
import static io.mh0rst.android.checktrackedprojects.Manifest.ProjectMap.fromEntries;
import static io.mh0rst.android.checktrackedprojects.Manifest.Source.fromURLs;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public enum Manifest {
    LOS14_1(fromURLs("https://raw.githubusercontent.com/LineageOS/android/cm-14.1/default.xml",
                     "https://raw.githubusercontent.com/LineageOS/android/cm-14.1/snippets/cm.xml"),
        fromEntries()), //
    CM13_0(
        fromURLs("https://raw.githubusercontent.com/LineageOS/android/cm-13.0/default.xml",
                 "https://raw.githubusercontent.com/LineageOS/android/cm-13.0/snippets/hal_cm_all.xml"),
        fromEntries(entry("hardware/qcom/audio/default", "hardware/qcom/audio"),
                    entry("hardware/qcom/media/default", "hardware/qcom/media")));

    Source sources;
    ProjectMap entries;

    private Manifest(Source sources, ProjectMap entries) {
        this.sources = sources;
        this.entries = entries;
    }

    static class Source {
        List<String> sources;

        static Source fromURLs(String... urls) {
            Source source = new Source();
            source.sources = Arrays.asList(urls);
            return source;
        }
    }

    static class ProjectMap {
        Map<String, String> map = new HashMap<>();

        @SafeVarargs
        static ProjectMap fromEntries(Entry<String, String>... entries) {
            ProjectMap pmap = new ProjectMap();
            for (Entry<String, String> entry : entries) {
                pmap.map.put(entry.getKey(), entry.getValue());
            }
            return pmap;
        }

        static Entry<String, String> entry(String key, String value) {
            return new AbstractMap.SimpleEntry<>(key, value);
        }
    }

}
