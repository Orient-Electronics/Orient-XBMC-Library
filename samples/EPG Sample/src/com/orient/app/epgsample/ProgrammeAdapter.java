package com.orient.app.epgsample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orient.lib.xbmc.epg.ProgrammeInfoTag;
import com.squareup.picasso.Picasso;

public class ProgrammeAdapter extends ArrayAdapter<ProgrammeInfoTag> {

	Context context; 
    int layoutResourceId;
	private ArrayList<ProgrammeInfoTag> programmeList;
    
	public ProgrammeAdapter(Context context, int layoutResourceId,
			ArrayList<ProgrammeInfoTag> programmeList) {
		super(context, layoutResourceId, programmeList);

		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.programmeList = programmeList;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		// Layout stuff
		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        view = inflater.inflate(layoutResourceId, parent, false);
        
        // TextView stuff
        ImageView image = (ImageView) view.findViewById(R.id.showThumb);
        TextView time = (TextView) view.findViewById(R.id.showTime);
        TextView title = (TextView) view.findViewById(R.id.showTitle);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.showProgressBar);
        
        // Setting Data
        ProgrammeInfoTag programme = (ProgrammeInfoTag) programmeList.get(position);
        
        title.setText(programme.info.title);
        
        if (programme.startTime != null) {
        	
        	SimpleDateFormat formatter = new SimpleDateFormat(
                    "h:mm a", Locale.getDefault());

        	time.setText(formatter.format(programme.startTime));
        }
        
        if (programme.info.pictureUrl != null && !programme.info.pictureUrl.urlList.isEmpty())
        	Picasso.with(context).load(programme.info.pictureUrl.urlList.get(0).url).into(image);
        

        if (programme.wasActive()) {
        	view.setBackgroundColor(0xffEEEEEE);
        	title.setTextColor(0xff999999);
        }
        
        if (programme.isActive()) {
        	progressBar.setProgress((int) programme.progressPercentage());
        }
        else
        	progressBar.setVisibility(View.GONE);
        
        return view;
	}
}
