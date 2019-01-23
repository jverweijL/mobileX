package com.liferay.shopx;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayOutputStream;

public class BarcodeFragment extends Fragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;
    Button button;
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_barcode, container, false);

        button = (Button) view.findViewById(R.id.takephoto);
        imageView = (ImageView) view.findViewById(R.id.result);

        System.out.println(button + " found");
        System.out.println(imageView + " found");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                Toast.makeText(getActivity(),"Don't touch this button!",Toast.LENGTH_SHORT).show();

            }
        });

        return view;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // convert byte array to Bitmap

                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);

                imageView.setImageBitmap(bitmap);

                BarcodeDetector detector =
                        new BarcodeDetector.Builder(getActivity().getApplicationContext())
                                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                                .build();
                if(!detector.isOperational()){
                    Toast.makeText(getActivity(),"Please try again in a minute!",Toast.LENGTH_SHORT).show();

                    return;
                } else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);

                    if (barcodes.size() > 0) {
                        Barcode thisCode = barcodes.valueAt(0);
                        Toast.makeText(getActivity(),"Code detected: " + thisCode.displayValue,Toast.LENGTH_SHORT).show();

                        Fragment fragment = new ProductsFragment();
                        Bundle args = new Bundle();
                        args.putString(ProductsFragment.ARG_PRODUCTID,thisCode.displayValue.replaceAll("http:\\/\\/",""));
                        fragment.setArguments(args);
                        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrame, fragment);
                        ft.commit();
                    } else {
                        Toast.makeText(getActivity(),"Didn't find the barcode!",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}