package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vuforia.samples.VuforiaSamples.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 泽林 on 2018/6/15.
 */

public class ChosedModelAdapter extends RecyclerView.Adapter<ChosedModelAdapter.ViewHolder>  implements View.OnClickListener {
    private List<ChosedModel> mFruitList = new ArrayList<ChosedModel>();

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(int) v.getTag());
        }
    }

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , int pos);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mMImageView;
        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mMImageView =  (ImageView) itemView.findViewById(R.id.fruit_image);
            mTextView = (TextView) itemView.findViewById(R.id.fruit_name);
        }
    }

    public ChosedModelAdapter(List<ChosedModel> fruitList) {
        mFruitList = fruitList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_chosedmodel, null);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChosedModelAdapter.ViewHolder holder, int position) {

        ChosedModel fruit = mFruitList.get(position);
        holder.mMImageView.setImageResource(fruit.getImageId());
        holder.mTextView.setText(fruit.getName());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }
}
