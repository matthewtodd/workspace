require 'fileutils'

class Filesystem
  def initialize(path)
    @path = path
  end

  def absolute_path(path = '.')
    File.absolute_path(File.join(@path, path))
  end

  def exists?(path)
    File.exist?(absolute_path(path))
  end

  def glob(pattern)
    Dir.glob(File.join(@path, pattern)).each do |path|
      yield relative_path(path), IO.read(path)
    end
  end

  def link(path, src)
    target = absolute_path(path)
    FileUtils.mkdir_p(File.dirname(target))
    FileUtils.ln(src, target, force: true)
  end

  def sandbox(*segments)
    Filesystem.new(File.join(@path, *segments))
  end

  private

  def relative_path(path)
    path.slice(@path.length.next..-1) || ''
  end
end
