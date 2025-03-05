package qdu.edu.cn.utils;

public class Progress {
    public static void progressBar(int sum, int num, String str)
    {
        int progress = (num * 100) / sum;
        StringBuilder progressBar = new StringBuilder(str + "[");
        int barLength = 50;  // 进度条长度
        int pos = (progress * barLength) / 100;
        // 填充进度条
        for (int j = 0; j < barLength; j++) {
            if (j < pos) {
                progressBar.append("#");  // 已完成部分
            } else {
                progressBar.append(" ");  // 未完成部分
            }
        }
        progressBar.append("] ");
        progressBar.append(progress);
        progressBar.append("%");
        System.out.print("\r" + progressBar.toString());
    }
}
