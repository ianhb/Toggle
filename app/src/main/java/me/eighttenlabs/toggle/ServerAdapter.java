package me.eighttenlabs.toggle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter to show the available servers on the network in ServerSelectActivity
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class ServerAdapter extends ArrayAdapter<Server> {

    ArrayList<Server> servers;

    public ServerAdapter(Context context, ArrayList<Server> availableServers) {
        super(context, R.layout.server, availableServers);
        servers = availableServers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.server, parent, false);
            holder = new Holder();
            holder.name = (TextView) convertView.findViewById(R.id.server_name);
            holder.ip = (TextView) convertView.findViewById(R.id.server_ip);
            convertView.setTag(holder);

        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.name.setText(servers.get(position).name);
        holder.ip.setText(servers.get(position).ip);

        return convertView;
    }

    public static class Holder {
        TextView name;
        TextView ip;
    }
}
