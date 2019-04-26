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
    private Context mContext;

    private String[] senseArr = {"DISABLED", "FACE_PRIORITY", "ACTION", "PORTRAIT", "LANDSCAPE", "NIGHT"
            , "NIGHT_PORTRAIT", "THEATRE", "BEACH", "SNOW", "SUNSET", "STEADYPHOTO", "FIREWORKS",
            "SPORTS", "PARTY", "CANDLELIGHT", "BARCODE"};

    public SenseAdapter(Context mContext) {
        this.mContext = mContext;
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
            Log.e("NormalTextViewHolder", "onClick--> position = " + getPosition());
        }
    }

    public SenseOnItemClickListener senseOnItemClickListener;

    public interface SenseOnItemClickListener {

        void itemOnClick(int position);

    }

    public SenseOnItemClickListener getSenseOnItemClickListener() {
        return senseOnItemClickListener;
    }

    public void setSenseOnItemClickListener(SenseOnItemClickListener senseOnItemClickListener) {
        this.senseOnItemClickListener = senseOnItemClickListener;
    }
}
