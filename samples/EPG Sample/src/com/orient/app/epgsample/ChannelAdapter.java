package com.orient.app.epgsample;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.orient.lib.xbmc.epg.ChannelInfoTag;
import com.squareup.picasso.Picasso;

public class ChannelAdapter extends ArrayAdapter<ChannelInfoTag> {
	
	Context context; 
    int layoutResourceId;    
    List<ChannelInfoTag> list = null;
    
	public ChannelAdapter(Context context, int layoutResourceId, List<ChannelInfoTag> list) {
		super(context, layoutResourceId, list);

		this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.list = list;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		// Layout stuff
		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        view = inflater.inflate(layoutResourceId, parent, false);
        
        // TextView stuff
        ImageView image = (ImageView) view.findViewById(R.id.channelListItemIcon);
        TextView name = (TextView) view.findViewById(R.id.channelListItemName);
        TextView id = (TextView) view.findViewById(R.id.channelListItemId);
        
        // Setting Data
        ChannelInfoTag channel = (ChannelInfoTag) list.get(position);
        
        name.setText(channel.name);
        id.setText(channel.id);
        
        if (channel.logoUrl != null)
        	Picasso.with(context).load(channel.logoUrl).into(image);
        
        return view;
	}
}
