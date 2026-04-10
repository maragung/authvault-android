#!/bin/bash

check_existing_swap() {
    echo "Checking existing swap area..."
    sudo swapon --show
}

resize_swap() {
    if [ -e /swapfile ]; then
        echo "Disabling the swap file before resizing..."
        sudo swapoff /swapfile

        read -p "Enter the new swap size in gigabytes (e.g., 4 for 4GB): " new_swap_size_gb
        new_swap_size_mb=$((new_swap_size_gb * 1024))

        echo "Resizing the swap file to $new_swap_size_gb GB..."
        sudo fallocate -l ${new_swap_size_mb}M /swapfile

        echo "Setting up swap area..."
        sudo mkswap /swapfile

        echo "Enabling the resized swap file..."
        sudo swapon /swapfile

        echo "Swap file has been resized and enabled."
        sudo swapon --show
    else
        echo "No swap file found. Please create a swap file first."
    fi
}


create_swap() {
    if [ -e /swapfile ]; then
        echo -e "Swap file already exists.\n"

        read -p "Do you want to resize the existing swap file? (y/n): " resize_option
        if [ "$resize_option" == "y" ] || [ "$resize_option" == "Y" ]; then
            resize_swap
        else
            echo "Aborting. No changes made to the existing swap file."
        fi
    else
        read -p "Enter the swap size in gigabytes (e.g., 2 for 2GB): " swap_size_gb
        swap_size_mb=$((swap_size_gb * 1024))

        echo "Creating a swap file of size $swap_size_gb GB..."
        sudo fallocate -l ${swap_size_mb}M /swapfile

        echo "Setting permissions on the swap file..."
        sudo chmod 600 /swapfile

        echo "Setting up swap area..."
        sudo mkswap /swapfile

        echo "Enabling the swap file..."
        sudo swapon /swapfile

        echo "Swap file has been created and enabled."
        sudo swapon --show

        # Add an entry for the swapfile in /etc/fstab
        echo "/swapfile swap swap defaults 0 0" | sudo tee -a /etc/fstab

        echo -e '\nSwap file added successfully.\n'
    fi
}

disable_swap() {
    echo "Disabling the swap file..."
    sudo swapoff /swapfile

    echo "Swap file has been disabled."
}

enable_swap() {
    echo "Enabling the swap file..."
    sudo swapon /swapfile

    echo "Swap file has been enabled."
    sudo swapon --show
}

# Main script
echo "Select an option:"
echo "1. Check Existing Swap Area"
echo "2. Create Swap Area"
echo "3. Resize Swap Area"
echo "4. Disable Swap Area"
echo "5. Enable Swap Area"
echo "6. Exit"

read -p "Enter your choice: " choice

case $choice in
    1) check_existing_swap ;;
    2) create_swap ;;
    3) resize_swap ;;
    4) disable_swap ;;
    5) enable_swap ;;
    6) echo "Exiting script. Goodbye!"; exit 0 ;;
    *) echo "Invalid choice. Exiting script."; exit 1 ;;
esac
