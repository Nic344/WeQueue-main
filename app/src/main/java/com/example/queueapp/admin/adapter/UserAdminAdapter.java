package com.example.queueapp.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.queueapp.R;
import com.example.queueapp.api.model.UserModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.UserViewHolder> implements Filterable {

    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> filteredUsers = new ArrayList<>();
    private final OnUserAdminClickListener listener;

    public interface OnUserAdminClickListener {
        void onChangeRoleClick(UserModel user, String newRole);
    }

    public UserAdminAdapter(OnUserAdminClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<UserModel> items) {
        this.allUsers = new ArrayList<>(items);
        this.filteredUsers = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = filteredUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<UserModel> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered.addAll(allUsers);
                } else {
                    for (UserModel item : allUsers) {
                        if (item.getName() != null && item.getName().toLowerCase().contains(query) ||
                            item.getEmail() != null && item.getEmail().toLowerCase().contains(query) ||
                            item.getRole() != null && item.getRole().toLowerCase().contains(query)) {
                            filtered.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredUsers.clear();
                if (results.values != null) {
                    filteredUsers.addAll((List<UserModel>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivUserAvatar;
        TextView tvUserName, tvUserEmail, tvUserRole;
        ImageButton btnChangeRole;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnChangeRole = itemView.findViewById(R.id.btnChangeRole);
        }

        void bind(UserModel user) {
            tvUserName.setText(user.getName() != null ? user.getName() : "Unknown");
            tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");
            
            String role = user.getRole() != null ? user.getRole() : "customer";
            tvUserRole.setText(role.toUpperCase());
            
            // Set role background color
            if ("admin".equalsIgnoreCase(role)) {
                tvUserRole.setBackgroundColor(Color.parseColor("#F44336")); // Red
            } else if ("staff".equalsIgnoreCase(role)) {
                tvUserRole.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
            } else {
                tvUserRole.setBackgroundColor(Color.parseColor("#9E9E9E")); // Grey
            }

            Glide.with(itemView.getContext())
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(ivUserAvatar);

            btnChangeRole.setOnClickListener(v -> {
                Context context = itemView.getContext();
                PopupMenu popup = new PopupMenu(context, btnChangeRole);
                popup.getMenu().add("Make Customer");
                popup.getMenu().add("Make Staff");
                popup.getMenu().add("Make Admin");
                
                popup.setOnMenuItemClickListener(item -> {
                    String newRole = "customer";
                    if (item.getTitle().equals("Make Staff")) newRole = "staff";
                    else if (item.getTitle().equals("Make Admin")) newRole = "admin";
                    
                    if (!newRole.equalsIgnoreCase(user.getRole()) && listener != null) {
                        listener.onChangeRoleClick(user, newRole);
                    }
                    return true;
                });
                popup.show();
            });
        }
    }
}
