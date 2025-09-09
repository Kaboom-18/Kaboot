package geq.kaboom.app.kaboot.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import geq.kaboom.app.kaboot.services.InstallService;
import geq.kaboom.app.kaboot.utils.KabUtil;
import geq.kaboom.app.kaboot.R;
import geq.kaboom.app.kaboot.utils.Config;

import java.util.ArrayList;
import java.util.HashMap;

public class InstallAdapter extends RecyclerView.Adapter<InstallAdapter.ViewHolder> {

  private final ArrayList<HashMap<String, Object>> data;
  private final AlertDialog dialog;
  private Context context;
  private KabUtil util;

  public InstallAdapter(AlertDialog dialog, ArrayList<HashMap<String, Object>> dataList) {
    this.dialog = dialog;
    this.data = dataList;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    context = parent.getContext();
    util = new KabUtil(context);
    View view = LayoutInflater.from(context).inflate(R.layout.install_pkg, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
    HashMap<String, Object> item = data.get(pos);
    final String name = item.get("name").toString();
    holder.name.setText(name);

    holder.name.setOnClickListener(
        v -> {
          if (!InstallService.isRunning()) {
            String url = item.get("url") != null ? item.get("url").toString() : null;
            if (url != null) {
              Intent intent = new Intent(context, InstallService.class);
              intent.putExtra(InstallService.EXTRA_NAME, name);
              intent.putExtra(InstallService.EXTRA_URL, url);
              ContextCompat.startForegroundService(context, intent);
               if (dialog.isShowing()) dialog.dismiss();
            } else {
              util.toast("Unsupported arch!");
            }
          } else {
            util.toast("Installer is busy!");
          }
        });
    holder.desc.setOnClickListener(
        (v) -> util.showDialog(name + " description", item.get("desc").toString()));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    ImageView desc;

    public ViewHolder(View v) {
      super(v);
      name = v.findViewById(R.id.name);
      desc = v.findViewById(R.id.desc);
    }
  }
}
