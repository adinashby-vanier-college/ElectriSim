package components;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

public class testComponent {
    private String type;
    private int data1;
    private int data2;
    private int data3;
    private int data4;

    public testComponent () {}
    public testComponent(String type, int data1, int data2, int data3, int data4) {
        this.type = type;
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.data4 = data4;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getData1() {
        return data1;
    }

    public void setData1(int data1) {
        this.data1 = data1;
    }

    public int getData2() {
        return data2;
    }

    public void setData2(int data2) {
        this.data2 = data2;
    }

    public int getData3() {
        return data3;
    }

    public void setData3(int data3) {
        this.data3 = data3;
    }

    public int getData4() {
        return data4;
    }

    public void setData4(int data4) {
        this.data4 = data4;
    }

    @Override
    public String toString() {
        return "testComponent{" +
                "type='" + type + '\'' +
                ", data1=" + data1 +
                ", data2=" + data2 +
                ", data3=" + data3 +
                ", data4=" + data4 +
                '}';
    }
}
