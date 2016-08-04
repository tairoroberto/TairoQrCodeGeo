package br.com.tairoroberto.tairoqrcodegeo.adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.tairoroberto.tairoqrcodegeo.MapsActivity;
import br.com.tairoroberto.tairoqrcodegeo.R;
import br.com.tairoroberto.tairoqrcodegeo.database.RegistroDAO;
import br.com.tairoroberto.tairoqrcodegeo.domain.Registro;
import br.com.tairoroberto.tairoqrcodegeo.interfaces.RecyclerViewOnClickListenerHack;


public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.ViewHolder> {
    private Context mContext;
    private List<Registro> mList;
    private LayoutInflater mLayoutInflater;
    private static RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;
    private static final String TAG = "Script";

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private Bitmap profile_imageBitmap;
    private Bitmap setphoto;

    private TimePickerDialog timepicker;
    private String hora;
    private String minuto;
    private String segundo;
    private Date dataRegistro;
    private FragmentManager fragmentManager;
    private RegistroDAO registroDAO;
    private ImageView img_registro;
    private Activity activity;


    public RegistroAdapter(Context context, List<Registro> list, FragmentManager fragmentManager){
        this.mContext = context;
        this.mList = list;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fragmentManager = fragmentManager;
        registroDAO = new RegistroDAO(mContext);
        activity = (Activity) mContext;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;

        view = mLayoutInflater.inflate(R.layout.item_registro_card, viewGroup, false);
        ViewHolder mvh = new ViewHolder(view);
        return mvh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder myViewHolder, final int position) {

        /** Separa a string "-" da data e coloca no textView */
        final Registro[] registro = {mList.get(position)};
        myViewHolder.txtQrcode.setText(registro[0].getContent());

        myViewHolder.txtQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regex = "http(s?)://([\\w-]+\\.)+[\\w-]+(/[\\w- ./]*)+\\.(?:[gG][iI][fF]|[jJ][pP][gG]|[jJ][pP][eE][gG]|[pP][nN][gG]|[bB][mM][pP])";

                Matcher m = Pattern.compile(regex).matcher(registro[0].getContent());

                if (m.find()){
                    saveImage(registro[0]);
                }else {
                    if (URLUtil.isValidUrl(registro[0].getContent())){
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(registro[0].getContent()));
                        mContext.startActivity(intent);
                    }else {
                        Toast.makeText(mContext, registro[0].getContent(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        /** Implementação do botão de deletar */
        myViewHolder.imgDeletar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registro[0] = mList.get(myViewHolder.getAdapterPosition());
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, R.style.AlertDialog);
                dialog.setTitle("Deseja relmente excluir?");
                dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        registroDAO.delete(registro[0]);
                        removeListItem(myViewHolder.getAdapterPosition());
                    }
                });
                dialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
            }
        });


        myViewHolder.imgMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MapsActivity.class);
                intent.putExtra("latitude", registro[0].getLatitude());
                intent.putExtra("longitude", registro[0].getLongitude());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    public void addListItem(Registro registro, int position){
        mList.add(registro);
        notifyItemInserted(position);
    }

    public void removeListItem(int position){
        mList.remove(position);
        notifyItemRemoved(position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imgMaps;
        public TextView txtQrcode;
        public ImageView imgDeletar;

        public ViewHolder(View itemView) {
            super(itemView);

            imgMaps = (ImageView) itemView.findViewById(R.id.imgMaps);
            txtQrcode = (TextView) itemView.findViewById(R.id.txtQrcode);
            imgDeletar = (ImageView) itemView.findViewById(R.id.imgDeletar);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null){
                mRecyclerViewOnClickListenerHack.onClickListener(v, getAdapterPosition());
            }
        }
    }

    public void saveImage(final Registro registro){

        String ext = registro.getContent().substring(registro.getContent().lastIndexOf('.') + 1);
        final String path = Environment.getExternalStorageDirectory().getPath()
                + "/Pictures/img_"+ registro.getId()+"." + ext;

        Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                File file = new File(path);
                try {
                    file.createNewFile();
                    FileOutputStream ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,ostream);
                    ostream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(mContext).load(registro.getContent()).into(mTarget);
        Toast.makeText(mContext, "Imagem salva no caminho: /Pictures/img_"+ registro.getId()+"." + ext , Toast.LENGTH_SHORT).show();
    }

}
