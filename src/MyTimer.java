public class MyTimer {
    Integer time = 0;
    MyTimer(){}
    public String addSecond(){
        time++;
        return String.format("%d%d:%d%d:%d%d",
                            time/3600/10,
                            time/3600%10,
                            time%3600/60/10,
                            time%3600/60%10,
                            time%3600%60/10,
                            time%3600%60%10);
    }
}
