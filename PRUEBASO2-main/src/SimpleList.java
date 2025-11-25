public class SimpleList<T> {
    private static class Node<E> {
        E value;
        Node<E> next;
        Node(E v){value=v;}
    }
    private Node<T> head;
    private int size;

    public void add(T value){
        Node<T> n=new Node<>(value);
        if(head==null){head=n;}else{
            Node<T> c=head;
            while(c.next!=null){c=c.next;}
            c.next=n;
        }
        size++;
    }

    public boolean remove(T value){
        Node<T> prev=null;Node<T> c=head;
        while(c!=null){
            if((c.value==null && value==null)|| (c.value!=null && c.value.equals(value))){
                if(prev==null){head=c.next;}else{prev.next=c.next;}
                size--;return true;
            }
            prev=c;c=c.next;
        }
        return false;
    }

    public T get(int index){
        if(index<0||index>=size) return null;
        Node<T> c=head;int i=0;
        while(c!=null){
            if(i==index) return c.value;
            c=c.next;i++;
        }
        return null;
    }

    public int size(){return size;}

    public void clear(){head=null;size=0;}

    public Object[] toArray(){
        Object[] arr=new Object[size];
        Node<T> c=head;int i=0;
        while(c!=null){arr[i]=c.value;i++;c=c.next;}
        return arr;
    }
}
