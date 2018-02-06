package com.example.charlesbai321.myapplication;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        int[] a = {2, 4, 8, 7, 10, 9, 8, 5};
        quicksort(a, 0, a.length-1);
        System.out.println(Arrays.toString(a));
    }

    void quicksort(int[] a, int low, int high){

        if(low < high){

            int pivot = a[high];
            int wall = low;

            for(int i = low; i < high; i++){
                if(a[i] <= pivot){
                    int temp = a[wall];
                    a[wall] = a[i];
                    a[i] = temp;
                    wall++;
                }
            }

            Integer temp = a[high];
            a[high] = a[wall];
            a[wall] = temp;
            quicksort(a, low, wall-1);
            quicksort(a, wall+1, high);
        }
    }
}