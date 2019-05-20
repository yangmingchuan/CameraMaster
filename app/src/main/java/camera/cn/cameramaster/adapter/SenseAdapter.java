package camera.cn.cameramaster.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import camera.cn.cameramaster.R;

/**
 * effect 适配器
 *
 * @packageName: cn.tongue.tonguecamera.adapter
 * @fileName: EffectAdapter
 * @date: 2019/4/17  14:33
 * @author: ymc
 * @QQ:745612618
 */

public class SenseAdapter extends RecyclerView.Adapter<SenseAdapter.EffectViewHolder> {
    private LayoutInflater mLayoutInflater;
    private String[] senseArr;

    public SenseAdapter(Context mContext,String[] arr) {
        this.senseArr = arr;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public EffectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EffectViewHolder(mLayoutInflater.inflate(R.layout.item_rv_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EffectViewHolder holder, int position) {
        holder.mTextView.setText(senseArr[position]);
    }

    @Override
    public int getItemCount() {
        return senseArr.length;
    }

    public class EffectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.text_view)
        TextView mTextView;

        EffectViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (senseOnItemClickListener != null) {
                senseOnItemClickListener.itemOnClick(getPosition());
            }
        }
    }

    private SenseOnItemClickListener senseOnItemClickListener;

    public interface SenseOnItemClickListener {

        void itemOnClick(int position);

    }

    public void setSenseOnItemClickListener(SenseOnItemClickListener senseOnItemClickListener) {
        this.senseOnItemClickListener = senseOnItemClickListener;
    }
}
