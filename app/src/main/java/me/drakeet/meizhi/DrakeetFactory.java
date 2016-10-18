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

package me.drakeet.meizhi;

/**
 * Created by drakeet on 8/9/15.
 */
public class DrakeetFactory {

    protected static final Object monitor = new Object();
    static GankApi sGankIOSingleton = null;
    static DrakeetApi sDrakeetSingleton = null;
    public static final int meizhiSize = 10;
    public static final int gankSize = 5;


    public static GankApi getGankIOSingleton() {   //-- 获取gankService单例，这里就可以直接从网上获取数据了
        synchronized (monitor) {
            if (sGankIOSingleton == null) {
                sGankIOSingleton = new DrakeetRetrofit().getGankService();
            }
            return sGankIOSingleton;
        }
    }


    public static DrakeetApi getDrakeetSingleton() { // 获取drakeetService单例
        synchronized (monitor) {
            if (sDrakeetSingleton == null) {
                sDrakeetSingleton = new DrakeetRetrofit().getDrakeetService();
            }
            return sDrakeetSingleton;
        }
    }
}
