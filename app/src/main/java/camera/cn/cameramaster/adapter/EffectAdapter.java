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

public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.EffectViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private String[] effectArr;

    public EffectAdapter(Context mContext,String[] arr) {
        this.mContext = mContext;
        this.effectArr = arr;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public EffectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EffectViewHolder(mLayoutInflater.inflate(R.layout.item_rv_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EffectViewHolder holder, int position) {
        holder.mTextView.setText(effectArr[position]);
    }

    @Override
    public int getItemCount() {
        return effectArr.length;
    }

    public class EffectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.text_view)
        TextView mTextView;

        EffectViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);

            Log.e("NormalTextViewHolder", "onClick--> position = " + getPosition());
        }

        @Override
        public void onClick(View v) {
            if (effectOnItemClickListener != null) {
                effectOnItemClickListener.itemOnClick(getPosition());
            }
        }
    }

    public EffectOnItemClickListener effectOnItemClickListener;

    public interface EffectOnItemClickListener {

        void itemOnClick(int position);

    }

    public EffectOnItemClickListener getEffectOnItemClickListener() {
        return effectOnItemClickListener;
    }

    public void setEffectOnItemClickListener(EffectOnItemClickListener effectOnItemClickListener) {
        this.effectOnItemClickListener = effectOnItemClickListener;
    }
}
