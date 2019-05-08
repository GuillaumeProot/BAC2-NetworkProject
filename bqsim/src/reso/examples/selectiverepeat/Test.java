package reso.examples.selectiverepeat;

import java.util.*;

public class Test {
    public static void main(String[] args){
        ArrayList<Integer> array=new ArrayList<>();
        array.add(3);
        array.add(1);
        array.add(2);
        array.add(4);
        array.add(5);
        array.add(0);
        //System.out.println(queue.poll());
        //System.out.println(queue.poll());
        //System.out.println(queue.poll());
        //System.out.println(queue.poll());
        //System.out.println(queue.poll());
        //System.out.println(queue.poll());
        //System.out.println(queue.poll());
        Iterator<Integer> it=array.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
        }
        Random r=new Random();
        while (true){
            System.out.println(r.nextFloat());
        }


    }
}
