/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.drakeet.meizhi.data.entity;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Table;
import java.util.Date;

/**
 * Created by drakeet on 6/20/15.
 */
@Table("meizhis") public class Meizhi extends Soul {

    @Column("url") public String url;                   // http://ww1.sinaimg.cn/large/610dc034jw1f867mvc6qjj20u00u0wh7.jpg
    @Column("type") public String type;                 // 福利
    @Column("desc") public String desc;                 // 9-26 冰蛙(Icefrog)的个人历史
    @Column("who") public String who;                   // daimajia
    @Column("used") public boolean used;                // true
    @Column("createdAt") public Date createdAt;         // Sun Sep 25 22:41:04 EDT 2016
    @Column("updatedAt") public Date updatedAt;         // null
    @Column("publishedAt") public Date publishedAt;     // Mon Sep 26 11:52:58 EDT 2016
    @Column("imageWidth") public int imageWidth;        // 0
    @Column("imageHeight") public int imageHeight;      // 0

    @Override
    public String toString() {
        return "Meizhi{" +
                super.toString() +
                "url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", desc='" + desc + '\'' +
                ", who='" + who + '\'' +
                ", used=" + used +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", publishedAt=" + publishedAt +
                ", imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                '}';
    }
}
