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

package me.drakeet.meizhi.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import me.drakeet.meizhi.service.AlarmReceiver;

/**
 * Created by drakeet on 7/1/15.
 */
public class AlarmManagers {

    public static void register(Context context) {
        Calendar today = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        //12:24:38 会
        today.set(Calendar.HOUR_OF_DAY, 12);
        today.set(Calendar.MINUTE, 24);
        today.set(Calendar.SECOND, 38);

        if (now.after(today)) {
            return;
        }

        //action 并不是唯一标识的，准确的要用setClass
        Intent intent = new Intent("me.drakeet.meizhi.alarm");
        intent.setClass(context, AlarmReceiver.class);
        // PendingIntent就是一个可以在满足一定条件下执行的Intent，它相比于Intent的优势在于自己携带有Context对象，这样他就不必依赖于某个activity才可以存在
        // 这个返回的对象可以被其他应用执行，即使当前应用结束掉
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 520, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // 闹钟服务
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //-- 当到了today的时间，会广播，相当于调用 sentBroadcast(intent)
        manager.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), broadcast);
    }
}
