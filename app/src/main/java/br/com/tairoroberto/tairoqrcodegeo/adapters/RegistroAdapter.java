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
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.tairoroberto.tairoqrcodegeo.R;
import br.com.tairoroberto.tairoqrcodegeo.database.RegistroDAO;
import br.com.tairoroberto.tairoqrcodegeo.domain.Registro;
import br.com.tairoroberto.tairoqrcodegeo.interfaces.RecyclerViewOnClickListenerHack;


public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.ViewHolder> implements PreferenceManager.OnActivityResultListener{
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
    private Registro registro;
    private RegistroDAO registroDAO;
    private ImageView img_registro;
    Activity activity;


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
        registro = mList.get(position);
        myViewHolder.txtQrcode.setText(registro.getContent());


        /** Implementação do botão de deletar */
        myViewHolder.imgDeletar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registro = mList.get(myViewHolder.getAdapterPosition());
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, R.style.AlertDialog);
                dialog.setTitle("Deseja relmente excluir?");
                dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        registroDAO.delete(registro);
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
                String path = "";

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                File folder = new File(Environment.getExternalStorageDirectory() + "/Worktime/img");

                if (!folder.exists()) {
                    folder.mkdir();
                }
                final Calendar c = Calendar.getInstance();
                String new_Date = c.get(Calendar.DAY_OF_MONTH) + "-"
                        + ((c.get(Calendar.MONTH)) + 1) + "-"
                        + c.get(Calendar.YEAR) + " " + c.get(Calendar.HOUR)
                        + "-" + c.get(Calendar.MINUTE) + "-"
                        + c.get(Calendar.SECOND);

                path = String.format(Environment.getExternalStorageDirectory() + "/Worktime/img/%s.png", "LoadImg(" + new_Date + ")");
                File photo = new File(path);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));

                // start the image capture Intent
                activity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            this.imageFromGallery(resultCode, data);

            img_registro.setImageBitmap(null);

            img_registro.setImageBitmap(setphoto);
        }
        return false;
    }

    private void imageFromGallery(int resultCode, Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = activity.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

        String profile_Path = cursor.getString(columnIndex);
        cursor.close();

        setphoto = BitmapFactory.decodeFile(profile_Path);

    }

    private void imageFromCamera(int resultCode, Intent data) {
        updateImageView((Bitmap) data.getExtras().get("data"));
    }

    private void updateImageView(Bitmap newImage) {
        setphoto = newImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader cursorLoader = new CursorLoader(mContext, uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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

}
