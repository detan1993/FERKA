package com.example.ferka.util;

import android.annotation.SuppressLint;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;

import java.lang.reflect.Field;

/**
 * Created by David on 25/3/2018.
 */

public class BottomNavigationViewHelper {

    @SuppressLint("RestrictedApi")
    public static void removeShiftMode(BottomNavigationView bottomNavigationView)
    {
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);

        try
        {
            Field shiftingMode = bottomNavigationMenuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(bottomNavigationMenuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < bottomNavigationMenuView.getChildCount(); i++)
            {
                BottomNavigationItemView item = (BottomNavigationItemView) bottomNavigationMenuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }
        }
        catch (NoSuchFieldException e)
        {
        }
        catch (IllegalAccessException e)
        {
        }
    }
}
