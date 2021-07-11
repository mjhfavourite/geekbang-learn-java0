package sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
* @author 逝水流/mjhfavourite@126.com
* @date 2021-03-05 16:50:19
* @version 1.0
* @description 
*
*/

public class Test {

	public static void main(String... args) {
		
		List<Integer> li = new LinkedList<Integer>();
		li.add(1);
		li.add(2);
		li.add(3);
		li.add(4);
		li.add(5);
		li.add(6);
		for (int i = 0;i < 6; i++) {
			System.out.println(li.get(0));
			li.remove(0);
		}
		

	}
}

