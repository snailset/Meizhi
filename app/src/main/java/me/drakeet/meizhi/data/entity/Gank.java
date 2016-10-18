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
 * Created by drakeet on 8/11/15.
 */
@Table("ganks") public class Gank extends Soul {

    @Column("url") public String url;                   // http://www.miaopai.com/show/oyhevpuGn7S5XIsgr6xqHQ__.htm
    @Column("type") public String type;                 // 休息视频
    @Column("desc") public String desc;                 // “故事的开头总是这样，适逢其会，猝不及防。”这是三对情侣的隔空对话，1分55秒恍然，3分47秒落泪。
    @Column("who") public String who;                   // lxxself
    @Column("used") public boolean used;                // true
    @Column("createdAt") public Date createdAt;         // Fri Sep 23 12:28:08 EDT 2016
    @Column("updatedAt") public Date updatedAt;         // null
    @Column("publishedAt") public Date publishedAt;     // Thu Oct 13 11:30:10 EDT 2016

    @Override
    public String toString() {
        return super.toString() +
                "Gank{" +
                "url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", desc='" + desc + '\'' +
                ", who='" + who + '\'' +
                ", used=" + used +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
