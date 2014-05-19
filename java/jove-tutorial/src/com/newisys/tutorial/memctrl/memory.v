module memory(input clk, input rw, input[7:0] addr, input[7:0] datain, output[7:0] dataout);

    integer i;
    reg[7:0] datareg;
    reg[7:0] mem[255:0];

    assign dataout = datareg;

    initial
    begin
        for(i = 0; i < 8'hff; i = i + 1)
        begin
            mem[i] = 0;
        end
    end

    always @(posedge clk)
    begin
        if(rw === 0)
            mem[addr] = datain;
        else if(rw === 1)
            datareg = mem[addr];
    end

endmodule
