require 'net/http'
require 'tempfile'

module Wake
  class Fetcher
    def initialize(filesystem)
      @cache = filesystem.sandbox('var/cache')
      @lib = filesystem.sandbox('var/lib')
    end

    def fetch(url, sha256, label, &extractor)
      raise unless label.path.start_with?('lib/')

      unless @cache.exists?(sha256)
        Net::HTTP.get_response(URI.parse(url)) do |response|
          Tempfile.open do |scratch|
            scratch.binmode
            response.read_body { |segment| scratch.write(segment) }
            scratch.flush

            if Digest::SHA256.file(scratch.path).hexdigest == sha256
              @cache.link(sha256, scratch.path)
            end
          end
        end
      end

      compressed = @cache.absolute_path(sha256)
      extracted = @lib.absolute_path(label.path.sub(%r{^lib/}, ''))

      if !File.exist?(extracted) || File.mtime(extracted) < File.mtime(compressed)
        extractor.call(compressed, extracted)
      end
    end
  end
end
