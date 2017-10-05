package com.example.cyc.downloadproject.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cyc.downloadproject.R;

import com.example.cyc.downloadproject.URL.URLDownload;
import com.example.cyc.downloadproject.Utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by cyc on 17-10-1.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder>implements
View.OnClickListener{

    public ArrayList<URLDownload>tasklist=null;
    private OnItemClickListener onItemClickListener=null;
    public static interface OnItemClickListener {
        void onItemClick(View view,int position);
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageButton downloadorpause;
        TextView filename;
        ImageView fileCategory;
        LinearLayout container;
        ProgressBar progressBar;
        TextView fileSizeTv;
        TextView speedTv;
        TextView downloadLength;

        public ViewHolder(View view){
            super(view);
            downloadorpause=(ImageButton) view.findViewById(R.id.download_pause);
            filename=(TextView)view.findViewById(R.id.file_name);
            fileCategory=(ImageView)view.findViewById(R.id.file_category);
            container=(LinearLayout)view.findViewById(R.id.ll_pb);
            progressBar=(ProgressBar)view.findViewById(R.id.progressBar);
            fileSizeTv=(TextView)view.findViewById(R.id.fileSize);
            speedTv=(TextView)view.findViewById(R.id.speed);
            downloadLength=(TextView)view.findViewById(R.id.downloadLength);
        }


    } public TaskAdapter(ArrayList<URLDownload> tasklist){
        this.tasklist=tasklist;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_download,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);

        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
       /* URLDownload task=tasklist.get(position);
        String filename=task.URLaddress.substring(task.URLaddress.lastIndexOf("/")+1);
        if(Utils.getCategory(filename)==Utils.APK){
            holder.fileCategory.setImageResource(R.drawable.ext_program);
        }else if (Utils.getCategory(filename)==Utils.TXT){
            holder.fileCategory.setImageResource(R.drawable.ext_text);
        }else if (Utils.getCategory(filename)==Utils.OTHER){
            holder.fileCategory.setImageResource(R.drawable.ext_other);
        }else if (Utils.getCategory(filename)==Utils.PIC){
            holder.fileCategory.setImageResource(R.drawable.ext_image);
        }else if (Utils.getCategory(filename)==Utils.ZIP){
            holder.fileCategory.setImageResource(R.drawable.ext_archive);
        }else if (Utils.getCategory(filename)==Utils.MP3){
            holder.fileCategory.setImageResource(R.drawable.ext_music);
        }else if (Utils.getCategory(filename)==Utils.MP4){
            holder.fileCategory.setImageResource(R.drawable.ext_video);
        }
        holder.filename.setText(filename);
        switch (task.state){
            case 1:
                holder.downloadorpause.setImageResource(R.drawable.stat_stop);
                break;
            case 2:
                holder.downloadorpause.setImageResource(R.drawable.stat_start);
                break;
            case 3:
                holder.downloadorpause.setImageResource(R.drawable.stat_full);
            default:break;

        }
        holder.downloadorpause.setOnClickListener(this);
        holder.downloadorpause.setTag(position);
        int progress=(int)(task.downloadLength*100.0/(task.fileSize*2));
        holder.progressBar.setProgress(progress);
        holder.speedTv.setText(Utils.getSpeed(task.speed));
        holder.fileSizeTv.setText(Utils.getFileSize(task.fileSize));
        holder.downloadLength.setText(Utils.getFileSize(task.downloadLength/2));
*/
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        URLDownload task=tasklist.get(position);
        if (payloads.isEmpty()){
            String filename=task.URLaddress.substring(task.URLaddress.lastIndexOf("/")+1);
            if(Utils.getCategory(filename)==Utils.APK){
                holder.fileCategory.setImageResource(R.drawable.ext_program);
            }else if (Utils.getCategory(filename)==Utils.TXT){
                holder.fileCategory.setImageResource(R.drawable.ext_text);
            }else if (Utils.getCategory(filename)==Utils.OTHER){
                holder.fileCategory.setImageResource(R.drawable.ext_other);
            }else if (Utils.getCategory(filename)==Utils.PIC){
                holder.fileCategory.setImageResource(R.drawable.ext_image);
            }else if (Utils.getCategory(filename)==Utils.ZIP){
                holder.fileCategory.setImageResource(R.drawable.ext_archive);
            }else if (Utils.getCategory(filename)==Utils.MP3){
                holder.fileCategory.setImageResource(R.drawable.ext_music);
            }else if (Utils.getCategory(filename)==Utils.MP4){
                holder.fileCategory.setImageResource(R.drawable.ext_video);
            }
            holder.filename.setText(filename);
            switch (task.state){
                case 1:
                    holder.downloadorpause.setImageResource(R.drawable.stat_stop);
                    break;
                case 2:
                    holder.downloadorpause.setImageResource(R.drawable.stat_start);
                    break;
                case 3:
                    holder.downloadorpause.setImageResource(R.drawable.stat_full);
                default:break;

            }
            holder.downloadorpause.setOnClickListener(this);
            holder.downloadorpause.setTag(position);
            int progress=(int)(task.downloadLength*100.0/(task.fileSize*2));
            holder.progressBar.setProgress(progress);
            holder.speedTv.setText(Utils.getSpeed(task.speed));
            holder.fileSizeTv.setText(Utils.getFileSize(task.fileSize));
            holder.downloadLength.setText(Utils.getFileSize(task.downloadLength/2));
        }
        else {
            int progress=(int)(task.downloadLength*100.0/(task.fileSize));
            holder.progressBar.setProgress(progress);
            holder.downloadLength.setText(Utils.getFileSize(task.downloadLength/2));
            holder.speedTv.setText(Utils.getSpeed(task.speed));
        }
    }


    @Override
    public void onClick(View view) {
        if (onItemClickListener!=null){
            //使用getTag()方法获得position
            onItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener=listener;
    }

    @Override
    public int getItemCount() {
        return tasklist.size();
    }
    public void updateView(String url,int progress,double speed){
        for (int i=0;i<tasklist.size();i++){
            URLDownload task=tasklist.get(i);
            if (task.URLaddress.equals(url)){
                task.setDownloadLength(progress);
                task.setSpeed(speed);
                notifyItemChanged(i,1);
            }
        }
    }
    public void updateViewArray(ArrayList<URLDownload> lists){
       /* if (lists.size()<tasklist.size()){
            notifyItemRemoved(tasklist.size()-1);
            this.tasklist=lists;
        }
        else if (lists.size()>tasklist.size()){
            this.tasklist=lists;
            notifyItemInserted(tasklist.size()-1);
        }*/
       this.tasklist=lists;
        notifyItemRangeChanged(0,tasklist.size());


    }
    public void updateViewSome(int position,ArrayList<URLDownload>lists){
        if (lists.size()<tasklist.size()){
            notifyItemRemoved(position);
            this.tasklist=lists;
        }
        else if (lists.size()>tasklist.size()){
            this.tasklist=lists;
            notifyItemInserted(tasklist.size());
        }
    }

}
