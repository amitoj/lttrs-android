#!/bin/env ruby

resolutions = {
	'mdpi' => 1,
	'hdpi' => 1.5,
	'xhdpi' => 2,
	'xxhdpi' => 3,
	'xxxhdpi' => 4,
}

def execute_cmd(cmd)
	puts cmd
	system cmd
end

images = {
    "cover.png" => ["cover.png", 304]
}

images.each do |source_filename, settings|

    output_filename, base_size = settings

    resolutions.each do |resolution, factor|
        path = "../res/drawable-#{resolution}/#{output_filename}"
        width = factor * base_size
        execute_cmd "convert #{source_filename} -resize #{width} #{path}"
    end
end
