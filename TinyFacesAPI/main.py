import requests
import os
from PIL import Image
import io

'''
This code makes 5 API requests with a limit of 50 avatars each, downloads all available images, and arranges them in a
grid with exactly 10 avatars per row. Each individual avatar is still saved separately in the avatars folder,
and they're all combined into a single merged_avatars.png file.
'''


def download_avatars_batch(limit=50, batch_num=1):
    """Download a batch of avatars, resize to 256x256, and return the image objects"""
    api_url = f"https://tinyfac.es/api/data?limit={limit}&quality=0"
    print(f"Fetching batch {batch_num} from API...")

    avatar_images = []
    successful_downloads = 0

    try:
        response = requests.get(api_url)
        response.raise_for_status()

        avatars_data = response.json()
        print(f"Found {len(avatars_data)} avatars in batch {batch_num}")

        for i, avatar in enumerate(avatars_data):
            try:
                avatar_url = avatar.get('url')
                if not avatar_url:
                    print(f"Avatar {i + 1} in batch {batch_num} has no URL, skipping")
                    continue

                avatar_id = avatar.get('id', f"avatar_b{batch_num}_{i}")
                filename = f"avatars/{avatar_id}.png"

                print(f"Downloading avatar {i + 1}/{len(avatars_data)} (batch {batch_num}): {avatar_url}")
                img_response = requests.get(avatar_url)
                img_response.raise_for_status()

                # Open the image
                img = Image.open(io.BytesIO(img_response.content))

                # Resize the image to 256x256
                img = img.resize((256, 256))

                # Save the resized image
                img.save(filename)

                # Add the resized image to the list for merging
                avatar_images.append(img)

                successful_downloads += 1
                print(f"Saved to {filename} (resized to 256x256)")

            except Exception as e:
                print(f"Error downloading avatar {i + 1} in batch {batch_num}: {e}")

    except Exception as e:
        print(f"Error with batch {batch_num}: {e}")

    return avatar_images, successful_downloads


def download_and_merge_avatars():
    # Create directory to save avatars
    if not os.path.exists("avatars"):
        os.makedirs("avatars")

    # Make 5 API requests
    all_avatar_images = []
    total_downloads = 0

    for batch in range(1, 6):
        batch_images, batch_downloads = download_avatars_batch(limit=50, batch_num=batch)
        all_avatar_images.extend(batch_images)
        total_downloads += batch_downloads
        print(f"Completed batch {batch}: {batch_downloads} avatars downloaded")

    # Merge avatars into a single image
    if total_downloads > 0:
        print(f"\nMerging {total_downloads} avatars into a single image...")

        # Get dimensions of first image (assuming all images have same dimensions)
        if all_avatar_images:
            img_width, img_height = all_avatar_images[0].size

            # Use 10 avatars per row
            grid_cols = 10
            grid_rows = (total_downloads + grid_cols - 1) // grid_cols  # Ceiling division

            # Create a new image with the right size for the grid
            merged_width = grid_cols * img_width
            merged_height = grid_rows * img_height
            merged_image = Image.new('RGBA', (merged_width, merged_height))

            # Paste each avatar onto the merged image
            for idx, img in enumerate(all_avatar_images):
                row = idx // grid_cols
                col = idx % grid_cols
                x_offset = col * img_width
                y_offset = row * img_height
                merged_image.paste(img, (x_offset, y_offset))

            # Save the merged image
            merged_filename = "avatars/merged_avatars.png"
            merged_image.save(merged_filename)

            print(f"Created merged image with {total_downloads} avatars")
            print(f"Merged image saved to: {os.path.abspath(merged_filename)}")

    print(f"\nDownload complete! Successfully downloaded {total_downloads} avatars to the 'avatars' folder.")


def add_my_picture_to_merged_avatars(my_picture_path):
    """
    Replace the last avatar in the merged image with a personal picture
    """
    try:
        # Set PIL to allow larger images
        Image.MAX_IMAGE_PIXELS = 1000000000  # Increase decompression bomb threshold

        # Check if merged image exists
        merged_path = "avatars/merged_avatars.png"
        if not os.path.exists(merged_path):
            print(f"Merged avatar file not found at {merged_path}")
            return False

        # Load the merged image
        merged_image = Image.open(merged_path)

        # Load and resize your personal image
        print(f"Loading your image from: {my_picture_path}")
        try:
            my_image = Image.open(my_picture_path)
        except Exception as e:
            print(f"Error loading image: {e}")
            return False

        # Get dimensions of existing avatars
        grid_cols = 10
        img_width = merged_image.width // grid_cols
        img_height = img_width  # Assuming square avatars

        # Calculate total number of complete avatars in the grid
        total_rows = merged_image.height // img_height
        total_cols = merged_image.width // img_width
        total_avatars = total_rows * grid_cols

        # Calculate the position of the last avatar
        last_row = (total_avatars - 1) // grid_cols
        last_col = (total_avatars - 1) % grid_cols

        print(f"Resizing your image to {img_width}x{img_height}")
        my_image = my_image.resize((img_width, img_height))

        # Calculate position for last avatar
        x_offset = last_col * img_width
        y_offset = last_row * img_height

        # Paste your image over the last avatar
        merged_image.paste(my_image, (x_offset, y_offset))

        # Save the updated merged image
        merged_image.save(merged_path)

        print(f"Successfully replaced the last avatar with your picture at position ({last_row}, {last_col})")
        return True

    except Exception as e:
        print(f"Error adding your picture: {e}")
        return False

# Example usage:
# add_my_picture_to_merged_avatars("path/to/your/photo.jpg")  # Adds at the end
# add_my_picture_to_merged_avatars("path/to/your/photo.jpg", (0, 0))  # Replaces top-left avatar


if __name__ == "__main__":
    download_and_merge_avatars()
    input("Press Enter to exit...")

